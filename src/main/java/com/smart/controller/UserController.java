package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.UserRepository;
import com.smart.dao.ContactRepository;
import com.smart.entities.ChangePassword;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userrepository;

	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data user
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String username = principal.getName();

		// get the user using user name
		User user = userrepository.getUserByUserName(username);
		m.addAttribute("user", user);
	}

	@GetMapping("/index")
	public String dashboard(Model m, Principal principal) {
		m.addAttribute("title", "Dashboard");
//		String username=principal.getName();

		// get the user using user name
//		User user = userrepository.getUserByUserName(username);
//		m.addAttribute("user",user);

		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String openAddContactForm(Model m) {
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute Contact contact, BindingResult errorResult, Model m,
			@RequestParam("profileImage") MultipartFile profile, Principal principal, HttpSession session) {

//		System.out.println(contact);
		try {

			if (errorResult.hasErrors()) {
				m.addAttribute("contact", contact);
				return "normal/add_contact_form";
			}

			// processing and uploading file

			if (profile.isEmpty()) {
				System.out.print("Image Empty");
				contact.setImage("contact.png");
			} else {
				// file to folder and update name
				contact.setImage(String.valueOf(Clock.systemDefaultZone().millis()) + profile.getOriginalFilename());
				File file = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(file.getAbsolutePath() + File.separator + contact.getImage());
				Files.copy(profile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.print("Image Uploaded");
			}

			String name = principal.getName();
			User user = this.userrepository.getUserByUserName(name);
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userrepository.save(user);

			m.addAttribute("contact", new Contact());
			session.setAttribute("message", new Message("Your contact is added !!", "alert-success"));

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Somthing went wrong !! please try again....", "alert-danger"));

		}

		return "normal/add_contact_form";
	}

	@GetMapping("/show-contacts/{page}")
	public String showContact(Model m, Principal pricipal, @PathVariable("page") Integer page) {
		m.addAttribute("title", "Show Contact");

		String name = pricipal.getName();
		User user = this.userrepository.getUserByUserName(name);
//		List<Contact> contacts = user.getContacts();

		// 2nd current page
		// 3rd
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPage", contacts.getTotalPages());

		return "normal/show_contact";
	}

	// shoving perticular contact
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model m, Principal principal) {

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		String name = principal.getName();
		User user = this.userrepository.getUserByUserName(name);

		if (user.getId() == contact.getUser().getId()) {
			m.addAttribute("contact", contact);
			m.addAttribute("title", contact.getName());
		}

		return "normal/contact_detail";
	}

	@PostMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model m, Principal principal, HttpSession session) {

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		String name = principal.getName();
		User user = this.userrepository.getUserByUserName(name);

		if (user.getId() == contact.getUser().getId()) {

			if (!contact.getImage().equals("contact.png")) {
				try {
					File file = new ClassPathResource("static/image").getFile();
					File profile = new File(file.getAbsolutePath() + File.separator + contact.getImage());
					boolean success = profile.delete();
				} catch (Exception e) {

				}
			}

			// remove connection from contact to user
			contact.setUser(null);
			this.contactRepository.delete(contact);
			session.setAttribute("message", new Message("Contact Delete Succesfully......", "alert-success"));
		}

		return "redirect:/user/show-contacts/0";

	}

	// open update form
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model m, Principal principal) {
		m.addAttribute("title", "Update Contact");

		Contact contact = this.contactRepository.findById(cId).get();

		String name = principal.getName();
		User user = this.userrepository.getUserByUserName(name);

		if (user.getId() == contact.getUser().getId()) {
			m.addAttribute("contact", contact);
		}

		return "normal/update_form";
	}

	// update contact handler
	@PostMapping("/process-update")
	public String updateContact(@Valid @ModelAttribute Contact contact, BindingResult errorResult,
			@RequestParam("profileImage") MultipartFile profile, Model m, Principal principal, HttpSession session) {

		try {

			String name = principal.getName();
			User user = this.userrepository.getUserByUserName(name);
			contact.setUser(user);
			// processing and uploading file

			Contact contactOld = this.contactRepository.findById(contact.getcId()).get();

			contact.setImage(contactOld.getImage());
			if (errorResult.hasErrors()) {
				m.addAttribute("contact", contact);
				return "normal/update_form";
			}

			if (profile.isEmpty()) {
//				System.out.print("Image Empty");
				contact.setImage(contactOld.getImage());
			} else {

				// delete old file
				if (!contactOld.getImage().equals("contact.png")) {
					File fileOld = new ClassPathResource("static/image").getFile();
					File profileOld = new File(fileOld.getAbsolutePath() + File.separator + contactOld.getImage());
					boolean success = profileOld.delete();
				}

				// file to folder and update name
				contact.setImage(String.valueOf(Clock.systemDefaultZone().millis()) + profile.getOriginalFilename());
				File file = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(file.getAbsolutePath() + File.separator + contact.getImage());
				Files.copy(profile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//				System.out.print("Image Uploaded");
			}
			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Contact Update Succesfully......", "alert-success"));

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Somthing went wrong !! please try again....", "alert-danger"));
			m.addAttribute("contact", contact);
			return "normal/update_form";

		}

		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	// Your profile
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		m.addAttribute("title", "Profile");
		return "normal/your_profile";
	}

	// open setting handler
	@GetMapping("/setting")
	public String openSetting(Model m) {
		m.addAttribute("title", "Setting");
		m.addAttribute("changePassword", new ChangePassword());
		return "normal/setting";
	}

	// change password handle
	@PostMapping("/change-password")
	public String chnagePassword(@Valid @ModelAttribute ChangePassword changePassword, BindingResult errorResult,Model m,
			Principal principal, HttpSession session) {
		String oldPass = changePassword.getOldPassword();
		String newPass = changePassword.getNewPassword();
		String conPass = changePassword.getConPassword();
		
//		System.out.println(oldPass + " " + newPass + " "+conPass);

		try {

			if (errorResult.hasErrors()) {
				m.addAttribute("changePassword", changePassword);
				return "normal/setting";
			}else if (newPass.equals(oldPass)) {
				session.setAttribute("message", new Message(
						"New password and old password is same !! , pleas enter different password.", "alert-danger"));
				return "redirect:/user/setting";
			}else if (!newPass.equals(conPass)) {
				session.setAttribute("message",
						new Message("New password and confirm password not match !!", "alert-danger"));
				return "redirect:/user/setting";
			}
			String userLogin = principal.getName();
			User user = this.userrepository.getUserByUserName(userLogin);

			if (this.bCryptPasswordEncoder.matches(oldPass, user.getPassword())) {
				user.setPassword(this.bCryptPasswordEncoder.encode(newPass));
				this.userrepository.save(user);
				session.setAttribute("message", new Message("Your password is changed....", "alert-success"));
			} else {
				session.setAttribute("message", new Message("Please enter correct password !!", "alert-danger"));
				return "redirect:/user/setting";
			}
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Somthing went wrong !! please try again....", "alert-danger"));
			m.addAttribute("changePassword", changePassword);
			return "redirect:/user/setting";

		}

		return "redirect:/user/index";
	}
	
}
