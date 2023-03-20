package com.example.demo.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Entity.Contact;
import com.example.demo.Entity.User;
import com.example.demo.dao.ContactRepository;
import com.example.demo.dao.UserRepository;
import com.example.demo.helper.Message;
import com.example.demo.services.UserService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	UserRepository userRepository;

	@Autowired
	ContactRepository contactRepository;

	User currentLogInUserDetails = null;

	// method form adding common data to response
	@ModelAttribute
	public void commondata(Model model, Principal principal) {
		String usename = principal.getName();
		System.out.println("User name=" + usename);
		currentLogInUserDetails = userRepository.loadUserByUsername(usename);
		System.out.println("User =" + currentLogInUserDetails);
		model.addAttribute("user", currentLogInUserDetails);
	}

	// home-dashboard
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "user/user_dashboard";
	}

	@GetMapping("/profile")
	public String showUserProfile() {

		return "user/profile";
	}

	@GetMapping("/add-contact-form")
	public String opencontactform(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "user/add-contact";

	}

	@PostMapping("/process-contact")
	public String processAddContact(@Valid @ModelAttribute Contact contact, BindingResult result,
			@RequestParam("profileImage") MultipartFile mpFile, Model model, HttpSession session) {

		Path destPath = null;
		String originalFilename = null;
		String currDateTime = (LocalDateTime.now() + "").replace(":", "-");
		try {

			if (mpFile.isEmpty()) {
				System.out.println("file is empty");
				originalFilename = "contact_profile.png";
			} else {
				originalFilename = currDateTime + "@" + mpFile.getOriginalFilename();
			}
			File savedFile = new ClassPathResource("/static/img").getFile();
			destPath = Paths.get(savedFile.getAbsolutePath() + File.separator + originalFilename);
			System.out.println("Image path :" + destPath);
			contact.setImage(originalFilename);
			contact.setUser(currentLogInUserDetails);
			currentLogInUserDetails.getContacts().add(contact);
			User addedContactResult = userService.addContactInUser(currentLogInUserDetails);

			if (addedContactResult != null) {
				Files.copy(mpFile.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("After successful contact added : " + addedContactResult);
			}

			session.setAttribute("message", new Message("Contact saved successfully.....!!", "success"));
			model.addAttribute("contact", new Contact());
			return "user/add-contact";

		} catch (Exception e) {

			System.out.println("Error : " + e);
			e.printStackTrace();
			model.addAttribute("contact", contact);
			session.setAttribute("message", new Message("Something goes wrong, please try again.....!!", "danger"));
			return "user/add-contact";
		}

	}

	@GetMapping("/setting")
	public String setting() {
		return "user/user-setting";
	}

	@SuppressWarnings("unused")
	@PostMapping("/change-setting")
	public String show_contacts(@ModelAttribute("user") @Valid User user, BindingResult bindingResult,
			@RequestParam("email") String email, @RequestParam("userprofile") MultipartFile mpFile, Model model,
			HttpSession session, Errors errors) {
		try {

			System.out.println("File Name =" + mpFile.getOriginalFilename());
			String originalFilename = null;
			Path destPath = null;
			String currDateTime = (LocalDateTime.now() + "").replace(":", "-");
			if (mpFile.isEmpty()) {
				System.out.println("file is empty");
				originalFilename = "contact_profile.png";
			} else {
				originalFilename = currDateTime + "@" + mpFile.getOriginalFilename();
			}
			File savedFile = new ClassPathResource("/static/img").getFile();
			destPath = Paths.get(savedFile.getAbsolutePath() + File.separator + originalFilename);
			System.out.println("Image path :" + destPath);
			currentLogInUserDetails.setImageUrl(originalFilename);
			if (bindingResult.hasErrors()) {
				return "user/user-setting";
			}

			User loadbyusername = userRepository.loadUserByUsername(email);
			System.out.println("user Email Id=" + email);
			if (loadbyusername.toString().isEmpty()) {
				return "user/user-setting";
			} else {
				System.out.println("updated user=" + user.toString());
				User result = userRepository.save(user);
				if (result != null) {
					Files.copy(mpFile.getInputStream(), destPath, StandardCopyOption.REPLACE_EXISTING);
					System.out.println("user successfully updated : " + result);
				}
				model.addAttribute("user", result);
				session.setAttribute("message", new Message("Updated successfully", "alert-success"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("ohhh..!" + e.getMessage(), "alert-warning"));
			System.out.println(e);
		}
		return "user/user-setting";
	}

//showing contact handler
	// per page=10
//current page=0	
	@GetMapping("/show-contacts/{page}")
	public String show_contact_form(@PathVariable("page") Integer page, Model model) {
		model.addAttribute("title", "show contact");
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contact = contactRepository.getContactsByUser(currentLogInUserDetails.getId(), pageable);
		model.addAttribute("contacts", contact);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPage", contact.getTotalPages());
		return "user/show_contact";

	}

	@GetMapping("/delete-contact/{cId}")
	public String delete(@PathVariable("cId") Integer cid, Model model) {
		System.out.println("Id=" + cid);
		Contact contact = userService.getContactById(cid);
		userService.deleteContact(currentLogInUserDetails, contact);
		System.out.println("Contact deleted SuccessFully");
		if (currentLogInUserDetails.getId() == contact.getUser().getId()) {
			this.userService.deleteContact(currentLogInUserDetails, contact);

		} else {
			model.addAttribute("message", new Message("You are not an authorized user for this contact", "denger"));
		}
		return "redirect:/user/show-contacts/0";
	}

	@GetMapping("/update-contact/{contactid}")
	public String updateContact(@PathVariable("contactid") Integer cId, Model model) {
		Contact contact = userService.getContactById(cId);

		model.addAttribute("title", "Update contact - Smart Contact Manager");
		model.addAttribute("subTitle", "Update your Contact");
		model.addAttribute("contact", contact);

		return "user/update_contact";
	}

	@SuppressWarnings("unused")
	@PostMapping("/process-update-contact")
	public String processUpdateContact(@ModelAttribute Contact contact,
			@RequestParam("profileImage") MultipartFile file, Model model, HttpSession session) {
		Contact oldContact = this.userService.getContactById(contact.getcId());

		try {

			contact.setUser(currentLogInUserDetails);
			File saveFile = new ClassPathResource("/static/img").getFile();
			String uniqueImageName = (LocalDateTime.now() + "").replace(":", "-") + "@" + file.getOriginalFilename();
			if (!file.isEmpty()) {

				if (oldContact.getImage() != null) {
					File deleteFile = new File(saveFile, oldContact.getImage());
					deleteFile.delete();
				}
				Path path = Paths.get(saveFile + File.separator + uniqueImageName);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(uniqueImageName);

			} else {
				if (oldContact.getImage() != null)
					contact.setImage(oldContact.getImage());
				else
					contact.setImage("contact_profile.png");

			}
			Contact updatedContact = this.userService.updateContactInUser(contact);
			session.setAttribute("message", new Message("Contact successfully updated", "success"));
		} catch (Exception e) {
			session.setAttribute("message", new Message("Contact updation failed ", "danger"));
			e.printStackTrace();
			model.addAttribute("contact", oldContact);
			return "redirect:/user/update-contact/" + contact.getcId();
		}

		System.out.println("contact name : " + contact.getName());
		System.out.println("contact ID : " + contact.getcId());
		return "redirect:/user/" + contact.getcId() + "/contact";
	}
	// Show User contact Id Detail

	@GetMapping("/{cId}/contact")
	public String showContact(@PathVariable("cId") int cId, Model model) {
		System.out.println("CID : " + cId);
		model.addAttribute("title", "Contact details : Smart contact Manager");
		Contact contactDetail = this.userService.getContactDetail(cId);
		if (!currentLogInUserDetails.getEmail().equals(contactDetail.getUser().getEmail()))
			model.addAttribute("message", new Message("You are not an authorized user for this contact", "denger"));
		else
			model.addAttribute("contact", contactDetail);

		return "user/show_user_contact_details";
	}

	// creating payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createorder(@RequestBody Map<String, Object> data) throws RazorpayException {

		int amt = Integer.parseInt(data.get("payment").toString());
		System.out.println("hey order function executed=" + amt);
		var client = new RazorpayClient("rzp_test_OjUYiPMUkioMby", "MICIzkaBpjfthfe2mGbIi5QN");
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("amount", amt * 100);
		jsonObject.put("currency", "INR");
		jsonObject.put("receipt", "TXN_2345565");
				// creating order
		Order order = client.Orders.create(jsonObject);
		System.out.println("order =" + order);
		//if you want stored in database
		return order.toString();
	}
}
