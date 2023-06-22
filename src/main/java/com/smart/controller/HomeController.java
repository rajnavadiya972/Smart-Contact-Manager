package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.Clock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userrepository;

	@GetMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home - Smart contact manager");
		return "home";
	}

	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "about - Smart contact manager");
		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register - Smart contact manager");
		m.addAttribute("user", new User());
		return "signup";
	}

	@PostMapping("/do_register")
	public String register(@Valid @ModelAttribute("user") User user, BindingResult errorResult,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement,
			@RequestParam("profileImage") MultipartFile profile, Model m, HttpSession session) {
		try {
			if (!agreement) {
				System.out.println("You have not agreed terms and condition");
				throw new Exception("You have not agreed terms and condition");
			}

			if (errorResult.hasErrors()) {
				m.addAttribute("user", user);
				return "signup";
			}

			if (profile.isEmpty()) {
				System.out.print("Image Empty");
				user.setImageUrl("user.png");
			} else {
				// file to folder and update name
				user.setImageUrl(String.valueOf(Clock.systemDefaultZone().millis()) + profile.getOriginalFilename());
				File file = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(file.getAbsolutePath() + File.separator + user.getImageUrl());
				Files.copy(profile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.print("Image Uploaded");
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);

			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println(agreement);
			System.out.println(user);

			User result = this.userrepository.save(user);

			m.addAttribute("user", new User());

			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong!! " + e.getMessage(), "alert-danger"));
			return "signup";
		}
	}

	@GetMapping("/signin")
	public String customLogin(Model m) {
		m.addAttribute("title", "Login - Smart contact manager");
		return "loginform";
	}

	@PostMapping("/update-user/{id}")
	public String updateForm(@PathVariable("id") Integer id, Model m, Principal principal) {
		m.addAttribute("title", "Update Contact");

		User user = this.userrepository.findById(id).get();

		m.addAttribute("user", user);

		return "update_detail";
	}

	@PostMapping("/do_update")
	public String updateContact(@Valid @ModelAttribute User user, BindingResult errorResult, Model m,
			@RequestParam("profileImage") MultipartFile profile, HttpSession session, Principal principal) {

		try {
			
			String username = principal.getName();
			// get the user using user name
			User userOld = userrepository.getUserByUserName(username);
			
			
			user.setImageUrl(userOld.getImageUrl());
			if (errorResult.hasErrors()) {
				m.addAttribute("user", user);
				return "update_detail";
			}

			// processing and uploading file

//			System.out.println(userOld);
			if (profile.isEmpty()) {
//				System.out.print("Image Empty");
				user.setImageUrl(userOld.getImageUrl());
			} else {

				// delete old file
				if (!userOld.getImageUrl().equals("user.png")) {
					File fileOld = new ClassPathResource("static/image").getFile();
					File profileOld = new File(fileOld.getAbsolutePath() + File.separator + userOld.getImageUrl());
					boolean success = profileOld.delete();
				}

				// file to folder and update name
				user.setImageUrl(String.valueOf(Clock.systemDefaultZone().millis()) + profile.getOriginalFilename());
				File file = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(file.getAbsolutePath() + File.separator + user.getImageUrl());
				Files.copy(profile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//				System.out.print("Image Uploaded");
			}
			user.setEnabled(userOld.isEnabled());
			user.setRole(userOld.getRole());
			user.setPassword(userOld.getPassword());
			user.setContacts(userOld.getContacts());
//			System.out.println(user);
			this.userrepository.save(user);

			session.setAttribute("message", new Message("User detail Update Succesfully......", "alert-success"));

		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message",
					new Message("Somthing went wrong !! please try again...." + e.getMessage(), "alert-danger"));
			return "update_detail";
		}

		return "redirect:/user/profile";
	}
}
