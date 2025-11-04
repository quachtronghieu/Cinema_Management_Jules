package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.entities.Staff;
import vn.edu.fpt.cinemamanagement.services.StaffService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.security.Principal;

@Controller
@RequestMapping("/staffs")
public class StaffController {

    // Thêm Phân trang
    @Autowired
    private StaffService staffService;

    // --- List all staff---
    @GetMapping
    public String getAllStaff(
            Model model,
            @RequestParam(name = "page", defaultValue = "1") int page) {

        int size = 5; // số staff mỗi trang
        int pageIndex = page - 1; // Spring Data bắt đầu từ 0
        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<Staff> staffPage = staffService.findAllStaff(pageable);
        model.addAttribute("staffList", staffPage.getContent());

        int totalPages = staffPage.getTotalPages();
        int currentPage = page;

        int visiblePages = 5;
        int startPage, endPage;

        if (totalPages <= visiblePages) {
            startPage = 1;
            endPage = totalPages;
        } else {
            startPage = ((currentPage - 1) / visiblePages) * visiblePages + 1;
            endPage = Math.min(startPage + visiblePages - 1, totalPages);
        }

        model.addAttribute("staffPage", staffPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);

        return "staffs/staff_list";
    }

    // --- Create form ---
    @GetMapping("/create")
    public String createStaff(Model model) {
       //add an empty Staff object to the model to bind form data
        model.addAttribute("staff", new Staff());
        return "staffs/staff_create";
    }

    // --- Show Create form  ---
    @PostMapping("/save")
    public String saveStaff(@ModelAttribute Staff staff, Model model) {
        // Check validation before save: GỌI HÀM TỪ SERVICE
        boolean hasError = staffService.validateStaff(staff, model, false);
        // If validation fails, return to the create form with error messages
        if (hasError) {
            model.addAttribute("staff", staff);
            return "staffs/staff_create"; // return  form create
        }
    // If validation passes, save the new staff to the database
        staffService.createStaff(staff);
        // Redirect to the staff list page
        return "redirect:/staffs";
    }
    // --- Show Update Staff Form - edit để id không tồn tại hiển thị thông báo---
    @GetMapping("/update/{id}")
    public String updateStaffForm(@PathVariable("id") String staffID, Model model) {
        // Get the existing staff by ID
        Staff staff = staffService.getStaffByID(staffID);
        if (staff == null) {
            // Tạo object trống để form vẫn render
            staff = new Staff();
            model.addAttribute("staff", staff);

            // Thêm thông báo lỗi vào model
            model.addAttribute("errorMessage", "Staff ID " + staffID + " done exist in the system.");
            return "staffs/staff_update"; // vẫn về form update
        }
        model.addAttribute("staff", staff);
        return "staffs/staff_update";
    }


    // --- Update Save ---
    @PostMapping("/update/{id}")
    public String updateStaff(@PathVariable("id") String staffID, @ModelAttribute Staff staff, Model model) {
        Staff existing = staffService.getStaffByID(staffID);
        if (existing == null) {
            return "redirect:/staffs";
        }

        // Giữ lại ID, phone, email, password cũ
        staff.setStaffID(existing.getStaffID());
        staff.setPhone(existing.getPhone());
        staff.setEmail(existing.getEmail());
        staff.setPassword(existing.getPassword());

        boolean hasError = staffService.validateStaff(staff, model, true);
        if (hasError) {
            model.addAttribute("staff", staff);
            return "staffs/staff_update";
        }

        staffService.updateStaff(staff);
        return "redirect:/staffs";
    }


    // --- Delete ---
    @PostMapping("/delete/{id}")
    public String deleteStaff(@PathVariable("id") String staffID) {
        staffService.deleteStaffByID(staffID);
        return "redirect:/staffs";
    }

    // --- Detail  - edit để báo thông báo khi id không tồn tại---
    @GetMapping("/detail/{id}")
    public String staffDetail(@PathVariable("id") String staffID, Model model) {
        Staff staff = staffService.getStaffByID(staffID);
        if (staff == null) {
            // Tạo object trống để form vẫn render
            staff = new Staff();
            model.addAttribute("staff", staff);

            // Thêm thông báo lỗi vào model
            model.addAttribute("errorMessage", "Staff ID " + staffID + " does not exist in the system .");
            return "staffs/staff_detail"; // vẫn về form update
        }
        model.addAttribute("staff", staff);
        return "staffs/staff_detail";
    }

    // ===========================
    // 🔹 6. VIEW STAFF PROFILE (SELF)
    // ===========================
    @GetMapping("/staff_profile")
    public String viewProfile(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String username = principal.getName(); // lấy username đăng nhập
        Staff staff = staffService.getAllStaff().stream()
                .filter(s -> s.getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (staff != null) {
            model.addAttribute("staff", staff);
        }
        return "staffs/staff_profile"; // file thymeleaf
    }

    // ===========================
    // 🔹 7. EDIT STAFF PROFILE (SELF)
    // ===========================
    @GetMapping("/staff_profile/edit/{id}")
    public String editProfile(@PathVariable("id") String id, Model model) {
        Staff staff = staffService.getStaffByID(id);
        if (staff != null) {
            model.addAttribute("staff", staff);
        }
        return "staffs/staff_update_profile";
    }

    // ===========================
    // 🔹 8. SAVE STAFF PROFILE (SELF)
    // ===========================
    @PostMapping("/staff_profile/save")
    public String saveProfile(@ModelAttribute Staff formStaff, Model model) {
        Staff existingStaff = staffService.getStaffByID(formStaff.getStaffID());
        if (existingStaff == null) {
            model.addAttribute("error", "Staff not found!");
            return "staffs/staff_update_profile";
        }

        existingStaff.setEmail(formStaff.getEmail());
        existingStaff.setPhone(formStaff.getPhone());

        boolean hasError = staffService.validateStaff(existingStaff, model, true);
        if (hasError) {
            model.addAttribute("staff", existingStaff);
            return "staffs/staff_update_profile";
        }

        staffService.getStaffRepo().save(existingStaff);
        return "redirect:/staffs/staff_profile";
    }
}