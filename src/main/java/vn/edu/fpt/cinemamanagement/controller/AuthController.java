package vn.edu.fpt.cinemamanagement.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.entities.Staff;
import vn.edu.fpt.cinemamanagement.services.AuthService;
import vn.edu.fpt.cinemamanagement.services.CustomerService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
public class AuthController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private AuthService authService;

    // ========================== LOGIN ==========================
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Display the login page
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password.");
        }
        return "auth/login";  // Render the login page
    }

    // Handle login submission
    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        boolean validLogin = authService.login(username, password);  // assuming this is how your service works
        if (!validLogin) {
            model.addAttribute("errorMessage", "Invalid username or password");
            return "auth/login";  // return to the login page with error message
        }
        return "redirect:/homepage";  // redirect to homepage after successful login
    }

    // ========================== REGISTER FORM (GET) ==========================
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("pageTitle", "Register");
        model.addAttribute("customer", new Customer());
        return "auth/register"; // trỏ đến templates/auth/register.html
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
}
