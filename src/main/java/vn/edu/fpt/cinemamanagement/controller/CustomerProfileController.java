package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.services.CustomerProfileService;

import java.security.Principal;

@Controller()
@RequestMapping("/profile")
public class CustomerProfileController {
    @Autowired
    private CustomerProfileService customerProfileService;

    @GetMapping("/my_profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            Customer customer = customerProfileService.getCustomerByUsername(username);
            model.addAttribute("customer", customer);
        }
        return "customers/my_profile";
    }

    @GetMapping("/edit")
    public String editProfileForm(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String username = principal.getName();
        Customer customer = customerProfileService.getCustomerByUsername(username);
        model.addAttribute("customer", customer); // trùng với th:object

        return "customers/update_profile"; // phải trùng với tên file Thymeleaf
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("customer") Customer customer, Model model) {
        boolean isValid = customerProfileService.validateCustomer(model, customer);

        if (!isValid) {
            model.addAttribute("customer", customer);
            return "customers/update_profile";
        }

        customerProfileService.save(customer);

        return "redirect:/profile/my_profile";
    }



}
