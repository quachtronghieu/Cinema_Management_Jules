package vn.edu.fpt.cinemamanagement.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.services.CustomerService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/customer")
public class CustomerController {
    private CustomerService customerService;
    @Autowired
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
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
