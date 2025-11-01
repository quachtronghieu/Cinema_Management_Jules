package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import vn.edu.fpt.cinemamanagement.entities.Staff;
import vn.edu.fpt.cinemamanagement.services.StaffService;
import org.springframework.ui.Model;
import java.security.Principal;

@Controller
public class StaffProfileController {
    @Autowired
    private StaffService staffService;
    // View profile
    @GetMapping("/profile/staff_profile")
    public String viewProfile(Model model, Principal principal) {
        String username = principal.getName(); // Lấy username đang đăng nhập
        Staff staff = staffService.getAllStaff().stream()
                .filter(s -> s.getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (staff != null) {
            model.addAttribute("staff", staff);
        }

        return "staffs/staff_profile"; // trỏ tới file Thymeleaf
    }

    // Update profile form
    @GetMapping("/profile/staff_update_profile/edit/{id}")
    public String editProfile(@PathVariable("id") String id, Model model) {
        Staff staff = staffService.getStaffByID(id);
        if (staff != null) {
            model.addAttribute("staff", staff);
        }
        return "staffs/staff_update_profile";
    }

    // save
    @PostMapping("/profile/staff_profile/save")
    public String saveProfile(@ModelAttribute Staff formStaff, Model model) {
        Staff existingStaff = staffService.getStaffByID(formStaff.getStaffID());
        if (existingStaff == null) {
            model.addAttribute("error", "Staff not found!");
            return "staffs/staff_update_profile";
        }

        // Update email & phone
        existingStaff.setEmail(formStaff.getEmail());
        existingStaff.setPhone(formStaff.getPhone());

        // Validate như bình thường
        boolean hasError = staffService.validateStaff(existingStaff, model, true);
        if (hasError) {
            model.addAttribute("staff", existingStaff);
            return "staffs/staff_update_profile";
        }

        // Save trực tiếp bằng repo, bỏ qua encode password**
        staffService.getStaffRepo().save(existingStaff); // Cần thêm getter cho staffRepo

        return "redirect:/profile/staff_profile";
    }
}

