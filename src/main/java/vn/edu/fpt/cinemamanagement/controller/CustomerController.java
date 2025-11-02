package vn.edu.fpt.cinemamanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.services.CustomerProfileService;
import vn.edu.fpt.cinemamanagement.services.CustomerService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller()
@RequestMapping("customer/profile")
public class CustomerController {
    @Autowired
    private CustomerService customerService;

    @GetMapping("/my_profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            Customer customer = customerService.getCustomerByUsername(username);
            model.addAttribute("customer", customer);
        }
        if (!model.containsAttribute("errors")) {
            model.addAttribute("errors", new HashMap<String, String>());
        }
        return "customers/my_profile";
    }

    @GetMapping("/edit")
    public String editProfileForm(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }

        String username = principal.getName();
        Customer customer = customerService.getCustomerByUsername(username);
        model.addAttribute("customer", customer); // trùng với th:object

        if (!model.containsAttribute("errors")) {
            model.addAttribute("errors", new HashMap<String, String>());
        }

        return "customers/update_profile"; // phải trùng với tên file Thymeleaf
    }


    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("customer") Customer customer, Model model) {
        Map<String, String> errors = customerService.validateCustomer(customer);

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("customer", customer);
            return "customers/update_profile";
        }

        customerService.save(customer);

        return "redirect:/profile/my_profile";
    }

    @GetMapping("changePassword")
    public String changePassword(Model model) {
        model.addAttribute("errors", new HashMap<String, String>());
        return "auth/change_password";
    }

    @PostMapping("/change")
    public String change(@RequestParam("currentPassword")  String currentPassword,
                         @RequestParam("newPassword") String newPassword,
                         @RequestParam("confirmPassword") String confirmPassword,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, String> errors = customerService.changePassword(username, currentPassword, newPassword, confirmPassword);

        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            model.addAttribute("currentPassword", currentPassword);
            model.addAttribute("newPassword", newPassword);
            model.addAttribute("confirmPassword", confirmPassword);

            return "auth/change_password";
        }
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());

        // ✅ Thông báo & chuyển hướng
        model.addAttribute("success", "Password changed successfully! Please log in again.");

        return "auth/login";
    }
}
