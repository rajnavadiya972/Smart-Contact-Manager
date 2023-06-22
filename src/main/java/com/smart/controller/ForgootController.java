package com.smart.controller;

import java.nio.channels.SeekableByteChannel;
import java.util.Optional;
import java.util.Random;

import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.ChangePassword;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class ForgootController {

	@Autowired
	private EmailService emailService;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	Random random = new Random(100000);

	// forget Password
	@GetMapping("/forgot-password")
	public String openEmailForm(Model m) {
		m.addAttribute("title", "Forgot Password");
		return "forget_password";
	}

	@PostMapping("/send-otp")
	public String sendOtp(@RequestParam("email") String email, HttpSession session,Model m) {
		m.addAttribute("title", "Forgot Password");
		try {

			Optional<User> isValid = this.userRepository.findByEmail(email);
			if (!isValid.isPresent()) {
				session.setAttribute("message",
						new Message("Email id not exist !! , Please enter right email id ..", "alert-danger"));
				return "redirect:/forgot-password";
			}
			
			int min = 100000; // Minimum 6-digit number
	        int max = 999999; // Maximum 6-digit number
	        int otp =  random.nextInt(max - min + 1) + min;
	        
//			System.out.println(otp);

			String subject = "OTP from Smart contact manager";
			String message = "<p>Your OTP code is: <h2><b>" + otp
					+ "</b></h2></p></br></br></br><p><font color='red'>Note:</font> Do not share with anyone.</p>";
			boolean flag = this.emailService.sendEmail(email, subject, message);

			if (flag) {
				session.setAttribute("message", new Message("We have sent OTP to your email...", "alert-success"));
				session.setAttribute("myOtp", otp);
				session.setAttribute("email", email);
				return "verify_otp";
			} else {
				session.setAttribute("message",
						new Message("Somthing went wrong !! please try again....", "alert-danger"));
				return "redirect:/forgot-password";
			}

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Somthing went wrong !! please try again....", "alert-danger"));
			return "redirect:/forgot-password";
		}
	}

	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") Integer otp, HttpSession session, Model m) {
		m.addAttribute("title", "Forgot Password");
		int myotp = (int) session.getAttribute("myOtp");
		String email = (String) session.getAttribute("email");
		if (myotp == otp) {

			User user = this.userRepository.getUserByUserName(email);
			if (user == null) {
				session.setAttribute("message",
						new Message("User not exist !! , Please enter right email id ..", "alert-danger"));
				return "forget_password";
			}

			session.removeAttribute("myOtp");
			m.addAttribute("changePassword", new ChangePassword());
			return "change_password";
		} else {
			session.setAttribute("message", new Message("Please Enter Correct OTP !!", "alert-danger"));
			return "verify_otp";
		}

	}

	@PostMapping("/change-login-password")
	public String verifyOtp(@Valid @ModelAttribute ChangePassword changePassword, BindingResult errorResult, Model m,
			HttpSession session) {
		m.addAttribute("title", "Forgot Password");
		String newPass = changePassword.getNewPassword();
		String conPass = changePassword.getConPassword();

//		System.out.println(oldPass + " " + newPass + " "+conPass);

		try {

			if (errorResult.hasErrors()) {
				m.addAttribute("changePassword", changePassword);
				return "change_password";
			} else if (!newPass.equals(conPass)) {
				session.setAttribute("message",
				new Message("New password and confirm password not match !!", "alert-danger"));
				return "change_password";
			}
			String email = (String) session.getAttribute("email");
			User user = this.userRepository.getUserByUserName(email);
			user.setPassword(this.bCryptPasswordEncoder.encode(newPass));
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Your password is changed....", "alert-success"));
			session.removeAttribute("email");
			return "redirect:/signin";
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("Somthing went wrong !! please try again....", "alert-danger"));
			m.addAttribute("changePassword", changePassword);
			return "change_password";

		}
	}
}
