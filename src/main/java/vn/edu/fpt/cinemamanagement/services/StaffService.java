package vn.edu.fpt.cinemamanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model; // Cần import Model
import vn.edu.fpt.cinemamanagement.entities.Staff;
import vn.edu.fpt.cinemamanagement.repositories.StaffRepository;


import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    @Autowired
    private StaffRepository staffRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Read All ---
    public List<Staff> getAllStaff() {
        return staffRepo.findAll();
    }

    // --- Read One ---
    public Staff getStaffByID(String staffID) {
        Optional<Staff> optionalStaff = staffRepo.findById(staffID);
        return optionalStaff.orElse(null);
    }

    public Page<Staff> findAllStaff(Pageable pageable) {
        return staffRepo.findAll(pageable);
    }

    // --- Create / Save ---
    public void createStaff(Staff staff) {
        if (staff.getStaffID() == null || staff.getStaffID().isEmpty()) {
            staff.setStaffID(generateNewStaffID());
        }
        if (staff.getPassword() != null && !staff.getPassword().isEmpty()) {
            staff.setPassword(encodePassword(staff.getPassword()));
        }
        staffRepo.save(staff);
    }

    // --- Update ---
    public void updateStaff(Staff staff) {
        if (staff.getPassword() != null && !staff.getPassword().isEmpty()) {
            staff.setPassword(encodePassword(staff.getPassword()));
        }
        if (staff.getStaffID() != null && staffRepo.existsById(staff.getStaffID())) {
            staffRepo.save(staff);
        }
    }



    // --- Delete ---
    public void deleteStaffByID(String staffID) {
        if (staffRepo.existsById(staffID)) {
            staffRepo.deleteById(staffID);
        }
    }

    // --- ID Generator ---
    private String generateNewStaffID() {
        Staff lastStaff = staffRepo.findTopByOrderByStaffIDDesc();
        if (lastStaff == null) {
            return "ST000001";
        }
        String lastID = lastStaff.getStaffID(); // Ex: ST0009
        int number = Integer.parseInt(lastID.substring(2)) + 1;
        return String.format("ST%06d", number); // -> ST0010
    }
    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);  // Use BCryptPasswordEncoder to hash the password
    }

    // ------Validate--------
    public boolean validateStaff(Staff staff, Model model, boolean isUpdate) {
        boolean hasError = false;

        // --- Username ---
        if (staff.getUsername() == null || staff.getUsername().isEmpty() ||
                !staff.getUsername().matches("^(?=.*[a-z])(?=.*[0-9])[a-z0-9]+$")) {
            // Username phải chứa chữ thường và số
            model.addAttribute("errorUsername", "Username must contain both lowercase letters and numbers");
            hasError = true;
        } else {
            // Kiểm tra trùng username
            List<Staff> staffsWithSameUsername = staffRepo.findAllByUsername(staff.getUsername());
            for (Staff s : staffsWithSameUsername) {
                if (!s.getStaffID().equals(staff.getStaffID())) { // ID khác thì báo lỗi
                    model.addAttribute("errorUsername", "Username already exists");
                    hasError = true;
                    break; // Không cần loop nữa
                }
            }
        }


// --- Full Name ---
        if (staff.getFullName() != null) {
            // 1. Loại bỏ khoảng trắng đầu/cuối và nhiều khoảng trắng liên tiếp
            String normalized = staff.getFullName().trim().replaceAll("\\s+", " ");

            // 2. Split thành các từ
            String[] words = normalized.split(" ");

            // 3. Kiểm tra số từ >= 2
            if (words.length < 2) {
                model.addAttribute("errorFullName", "Full Name must contain at least 2 words");
                hasError = true;
            }

            // 4. Kiểm tra mỗi từ chỉ chữ cái
            boolean valid = true;
            for (int i = 0; i < words.length; i++) {
                if (!words[i].matches("^[A-Za-z]+$")) {
                    valid = false;
                    break;
                }
                // Viết hoa chữ cái đầu, chữ thường phần còn lại
                words[i] = words[i].substring(0,1).toUpperCase() + words[i].substring(1).toLowerCase();
            }

            if (!valid) {
                model.addAttribute("errorFullName", "Full Name cannot contain numbers or special characters");
                hasError = true;
            }

            // 5. Gán lại fullName đã chuẩn hóa
            staff.setFullName(String.join(" ", words));
        } else {
            model.addAttribute("errorFullName", "Full Name cannot be empty");
            hasError = true;
        }



        // --- Password ----
        boolean shouldValidatePassword = !isUpdate ||
                (staff.getStaffID() != null && getStaffByID(staff.getStaffID()) != null && // Dùng getStaffByID() của Service
                        !staff.getPassword().equals("*".repeat(getStaffByID(staff.getStaffID()).getPassword().length())));

        if (shouldValidatePassword) {
            if (staff.getPassword() == null || staff.getPassword().isEmpty() ||
                    !staff.getPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{6,}$")) {
                model.addAttribute("errorPassword", "Password must be ≥6 chars with upper, lower, number & symbol");
                hasError = true;
            }
        }

        // --- Phone ---
        if (staff.getPhone() == null || staff.getPhone().isEmpty() ||
                !staff.getPhone().matches("^0\\d{9}$")) {
            model.addAttribute("errorPhone", "Phone must be 10 digits starting with 0");
            hasError = true;
        } else {
            // Check uniqueness
            Staff existingByPhone = staffRepo.findByPhone(staff.getPhone());
            if (existingByPhone != null) {
                if (staff.getStaffID() == null || !existingByPhone.getStaffID().equals(staff.getStaffID())) {
                    model.addAttribute("errorPhone", "Phone already exists");
                    hasError = true;
                }
            }
        }

// --- Email ---
        if (staff.getEmail() != null) {
            // Chuyển email về chữ thường
            staff.setEmail(staff.getEmail().toLowerCase());
        }

        if (staff.getEmail() == null || staff.getEmail().isEmpty() ||
                !staff.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) {
            model.addAttribute("errorEmail", "Invalid email format");
            hasError = true;
        } else {
            // Check uniqueness
            Staff existingByEmail = staffRepo.findByEmail(staff.getEmail());
            if (existingByEmail != null) {
                if (staff.getStaffID() == null || !existingByEmail.getStaffID().equals(staff.getStaffID())) {
                    model.addAttribute("errorEmail", "Email already exists");
                    hasError = true;
                }
            }
        }

        return hasError;
    }
}
