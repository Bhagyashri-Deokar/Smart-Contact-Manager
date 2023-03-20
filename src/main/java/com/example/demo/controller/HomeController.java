package com.example.demo.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Entity.User;
import com.example.demo.Mail.EmailSenderService;
import com.example.demo.dao.UserRepository;
import com.example.demo.helper.Message;

@Controller
public class HomeController {
	@Autowired
	UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private EmailSenderService senderService;

	@RequestMapping("/home")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// handler for register user
	@SuppressWarnings("unused")
	@RequestMapping(value = "/do_register", method = RequestMethod.POST)
	public String do_register(@Valid @ModelAttribute("user") User user, BindingResult bindingResult,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {
		try {
			if (bindingResult.hasErrors()) {
				System.out.println("Error : " + bindingResult);
				return "signup";
			}
			if (!agreement) {
				System.out.println("You have not follow term and condition");
				throw new Exception("You have not follow term and condition");
			} else {
				user.setRole("ROLE_USER");
				user.setEnabled(agreement);
				user.setImageUrl("image.png");
				user.setPassword(passwordEncoder.encode(user.getPassword()));
			}
			System.out.println(agreement);
			System.out.println(user.toString());
			User result = userRepository.save(user);
			User emptyUser = new User();
			model.addAttribute("user", emptyUser);
			session.setAttribute("message", new Message("Registration successfully", "alert-success"));

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("ohhh..!" + e.getMessage(), "alert-warning"));

		}
		return "signup";

	}

	@GetMapping("/signin")
	public String login(Model model) {
		model.addAttribute("title", "Login : Smart contact Manager");
		return "login";
	}

	@GetMapping("/forgetPassword")
	public String forgetPassword(Model model) {
		model.addAttribute("status", true);
		model.addAttribute("pwdstatus", true);
		return "forget";
	}

	@GetMapping("/sendOtp")
	public String sendOtpp(Model model)
	{
		model.addAttribute("status", true);
		model.addAttribute("pwdstatus", true);
		return "forget";
	}

	@PostMapping("/sendOtp")
	public String sendOtp(@Valid @ModelAttribute("user") User userforupdatepwd,BindingResult bindingResult, Model model,HttpSession session) {
		try {
			String otp="";
			User user = userRepository.loadUserByUsername(userforupdatepwd.getEmail());
			System.out.println("USER=" + user+" empty ="+(userforupdatepwd.getOtp().isEmpty()));
			if (user==null) 
			{
				System.out.println("user not found");
				session.setAttribute("message", new Message("Ohhh User Is Invalid !!!", "alert-warning"));
				model.addAttribute("email", userforupdatepwd.getEmail());
				model.addAttribute("status", true);
				model.addAttribute("user",user);
				model.addAttribute("pwdstatus", true);
				
			} 
			else if (user != null && !(userforupdatepwd.getOtp().isEmpty())) 
			{
				String dbOtp=user.getOtp();
				System.out.println(userforupdatepwd.getOtp() +" And "+dbOtp+"==>"+userforupdatepwd.getOtp().equalsIgnoreCase(dbOtp));
				if(dbOtp.equalsIgnoreCase(userforupdatepwd.getOtp()) && user.getEmail().equalsIgnoreCase(userforupdatepwd.getEmail()))
				{
					
					System.out.println("User can set password");
					model.addAttribute("pwdstatus", false);
					model.addAttribute("email",userforupdatepwd.getEmail());
					model.addAttribute("user",user);
					model.addAttribute("status",true);
				}
				
			}
			else if(user!=null && !(userforupdatepwd.getPassword().isEmpty()))
			{
				try
				{
				System.out.println("User can set password");
				model.addAttribute("pwdstatus", false);
				model.addAttribute("user",user);
				model.addAttribute("status",true);
				System.out.println("Updated password="+userforupdatepwd.getPassword());
				user.setPassword(passwordEncoder.encode(userforupdatepwd.getPassword()));
				userRepository.save(user);
				System.out.println("Password updated");
				session.setAttribute("message", new Message("Password Change Succefully!!!", "alert-success"));
				
				}
				catch (Exception e)
				{
				System.out.println("You got Exception");
				}
				
				
			}
			else if(user != null && (userforupdatepwd.getOtp().isEmpty()))
			{
				System.out.println("User is Not Null and otp is not generated");
				otp = getAlphaNumericString(8);
				System.out.println("OTP=" + otp);
				System.out.println("User=" + user);
				senderService.sendMail(userforupdatepwd.getEmail(), "Your Verification code",
						"Verification code\nPlease use the verification code below to sign in.\n" + otp
								+ "\nIf you didn’t request this, you can ignore this email.");
				user.setOtp(otp);
				userRepository.save(user);
				model.addAttribute("status", false);
				model.addAttribute("pwdstatus", true);
				model.addAttribute("email", userforupdatepwd.getEmail());
				model.addAttribute("user",user);
			}
			else
			{
				model.addAttribute("status", true);
				model.addAttribute("pwdstatus", true);
				model.addAttribute("email", userforupdatepwd.getEmail());
				model.addAttribute("user",user);
			}
			
		} catch (Exception e) 
		{
			System.out.println("You got Exception ="+e);
			model.addAttribute("status", true);
			model.addAttribute("pwdstatus", true);
			model.addAttribute("email", userforupdatepwd.getEmail());
		}
		return "forget";
		
	}

	public static String getAlphaNumericString(int n) {

		// chose a Character random from this String
		String AlphaNumericString = "0123456789" + "abcdefghijklmnopqrstuvxyz";

		// create StringBuffer size of AlphaNumericString
		StringBuilder sb = new StringBuilder(n);

		for (int i = 0; i < n; i++) {

			// generate a random number between
			// 0 to AlphaNumericString variable length
			int index = (int) (AlphaNumericString.length() * Math.random());

			// add Character one by one in end of sb
			sb.append(AlphaNumericString.charAt(index));
		}
		return sb.toString();
	}

}
