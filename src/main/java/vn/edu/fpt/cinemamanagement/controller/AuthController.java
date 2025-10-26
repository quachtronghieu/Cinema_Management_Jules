package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.services.CustomerService;

import java.util.Map;

@Controller
public class AuthController {
    @Autowired
    private CustomerService customerService;

    @RequestMapping(value = "/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "auth/register";
    }

    @PostMapping(value = "/register")
    public String register(@ModelAttribute Customer customer,
                           @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                           @RequestParam(value = "agreedToTerms", required = false, defaultValue = "false") Boolean agreedToTerms,
                           Model model) {
        // Kiểm tra đồng ý điều khoản
        if (!agreedToTerms) {
            model.addAttribute("errors", Map.of("terms", "You must agree to the terms of service."));
            model.addAttribute("customer", customer);
            return "auth/register";
        }
        // Gọi service để validate và đăng ký
        Map<String, String> errors = customerService.registerCustomer(customer, confirmPassword);

        if (!errors.isEmpty()) {
            // Có lỗi - trả về form với thông báo lỗi
            model.addAttribute("errors", errors);
            model.addAttribute("customer", customer);
            return "auth/register";
        }

        // Thành công - redirect về homepage
        return "redirect:/homepage";
    }

    @GetMapping("forget_password")
    public String showForgetPasswordForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "auth/forget_password";
    }

    @PostMapping("forget_password")
    public String forgetPassword(Customer customer, Model model) {
        boolean isAvailable = customerService.checkAvailableEmail(customer.getEmail(), model);
        if (!isAvailable) {
            model.addAttribute("email", customer.getEmail() );
            return "auth/forget_password";
        }
        return "redirect:/homepage";
    }
}
