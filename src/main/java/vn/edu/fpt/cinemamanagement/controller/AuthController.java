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
import vn.edu.fpt.cinemamanagement.services.MailService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.UUID;

@Controller
public class AuthController {
    @Autowired
    private CustomerService customerService;
    @Autowired
    private AuthService authService;
    @Autowired
    private MailController  mailController;
    @Autowired
    private MailService mailService;

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
        customer.setVerify("active");

        // Thành công - redirect về homepage
        return "redirect:/homepage";
    }

    @GetMapping("/forget_password")
    public String showForgetPasswordForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "auth/forget_password";
    }

    @PostMapping("/forget_password")
    public String forgetPassword(@RequestParam("email") String email, Model model) {

        Customer customer = customerService.findCustomerByEmail(email);
        if (customer == null) {
            model.addAttribute("errorEmail", "No account found for this email!");
            model.addAttribute("customer", new Customer());
            return "auth/forget_password";
        }
        String input = "wait" + customer.getUser_id();
        String fi;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            fi = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        String title = "Reset Your Password";
        String link = "http://localhost:8080/veryAccount/done/" + customer.getUser_id() + "/" + fi;


        String content =
                "<div style='font-family: Arial, sans-serif; line-height: 1.6; color: #333;'>" +
                        "<h2>Hello " + customer.getUsername() + ",</h2>" +
                        "<p>We received a request to reset your CGV Movies account password.</p>" +
                        "<p>To set a new password, please click the button below:</p>" +
                        "<p><a href='" + link + "' style='background-color:#9D1212;color:white;padding:10px 20px;text-decoration:none;border-radius:6px;display:inline-block;'>Reset Password</a></p>" +
                        "<p>This link will expire soon for your account’s security.</p>" +
                        "<hr><p>Best regards,<br>The CGV Movies Team</p>" +
                        "</div>";


        mailService.sendForgetPasswordMail(title, content, customer.getEmail());


        return "auth/send_mail";
    }
}
