package vn.edu.fpt.cinemamanagement.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @GetMapping("")
    public String dashboard(Authentication authentication) {
        // Check if the user has the 'ADMIN' role
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            // If the user doesn't have the 'ADMIN' role, redirect to an access-denied page
            return "redirect:/access-denied";
        }

        // If the user has the 'ADMIN' role, allow access to the dashboard
        return "dashboard/dashboard";  // Render the dashboard view
    }

    @GetMapping("/seat")
    public String seat(HttpServletRequest request, Model model) {
        // ===== LẤY ROLE TỪ COOKIE =====
        String role = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("user_role".equals(cookie.getName())) {
                    role = cookie.getValue();
                    break;
                }
            }
        }

        // ===== KIỂM TRA ROLE =====
        if (role == null || !role.toLowerCase().contains("admin")) {
            return "redirect:/access-denied";
        }

        // Nếu là admin -> cho truy cập trang seat
        return "seats/seat";
    }
}
