package vn.edu.fpt.cinemamanagement.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaffHomeController {

    @GetMapping("/staff_home")
    public String staffHome(HttpServletRequest request, Model model) {
        // Lấy cookie role (nếu cần hiển thị)
        String role = "Staff";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("user_role".equals(c.getName())) {
                    role = c.getValue().replace("%20", " ");
                }
            }
        }

        model.addAttribute("role", role);
        return "dashboard/staff_home"; // đúng với vị trí file bạn đã để
    }
}
