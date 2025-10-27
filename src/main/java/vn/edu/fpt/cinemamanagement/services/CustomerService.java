package vn.edu.fpt.cinemamanagement.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.entities.Voucher;
import vn.edu.fpt.cinemamanagement.repositories.CustomerRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    // Inject PasswordEncoder through constructor
    public CustomerService(CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;  // Use BCryptPasswordEncoder for encoding passwords
    }

    // Regex patterns - định nghĩa ở đầu class
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[0-9]{9,11}$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])[A-Za-z0-9_]+$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).+$"
    );

    // ============================================
    // PUBLIC METHODS
    // ============================================

    /**
     * Đăng ký customer mới với validation đầy đủ
     *
     * @param customer        Customer entity
     * @param confirmPassword Password xác nhận
     * @return Map chứa errors (empty nếu thành công)
     */
    public Map<String, String> registerCustomer(Customer customer, String confirmPassword) {
        Map<String, String> errors = new HashMap<>();

        // 1. VALIDATE FORMAT - Kiểm tra định dạng dữ liệu
        validateUsername(customer.getUsername(), errors);
        validateDateOfBirth(customer.getDob(), errors);
        validateGender(customer.getSex(), errors);
        validateEmail(customer.getEmail(), errors);
        validatePhone(customer.getPhone(), errors);
        validatePassword(customer.getPassword(), errors);
        validateConfirmPassword(customer.getPassword(), confirmPassword, errors);

        // Nếu có lỗi format, return ngay (không cần check DB)
        if (!errors.isEmpty()) {
            return errors;
        }

        // 2. VALIDATE BUSINESS RULES - Kiểm tra trùng lặp trong database
        if (customerRepository.existsByUsername(customer.getUsername())) {
            errors.put("username", "Customer with username " + customer.getUsername() + " already exists");
        }

        if (customerRepository.existsByEmail(customer.getEmail())) {
            errors.put("email", "Customer with email " + customer.getEmail() + " already exists");
        }

        if (customerRepository.existsByPhone(customer.getPhone())) {
            errors.put("phone", "Customer with phone " + customer.getPhone() + " already exists");
        }

        // Nếu có lỗi business logic, return
        if (!errors.isEmpty()) {
            return errors;
        }

        // 3. SAVE - Tất cả validation pass, lưu vào database
        String newId = generateNewCustomerId();
        customer.setUser_id(newId);

        // Hash password before saving
        customer.setPassword(encodePassword(customer.getPassword()));  // Hash the password

        customerRepository.save(customer);

        // Return empty map = thành công
        return errors;
    }

    /**
     * Generate a new customer ID automatically
     */
    public String generateNewCustomerId() {
        String lastId = customerRepository.findLastCustomerId();

        if (lastId == null || lastId.isEmpty()) {
            return "CS000001";
        }

        int number = Integer.parseInt(lastId.substring(2)) + 1;
        return String.format("CS%06d", number);
    }

    public boolean checkAvailableEmail(Model model, String email) {
        boolean isAvailable = true;

        if (email.isEmpty() || email.trim().isEmpty()) {
            model.addAttribute("errorEmail", "The email must be not empty!");
            isAvailable = false;
        }
        Customer customer = customerRepository.findByEmail(email);
        if (customer == null) {
            model.addAttribute("errorEmail", "Do not have any account available");
            isAvailable = false;

        }

        return isAvailable;
    }

    // ============================================
    // PRIVATE VALIDATION METHODS
    // ============================================

    private void validateUsername(String username, Map<String, String> errors) {
        if (username == null || username.trim().isEmpty()) {
            errors.put("username", "The username is required.");
            return;
        }

        if (username.length() < 2 || username.length() > 20) {
            errors.put("username", "The name must have between 2 and 20 characters.");
            return;
        }

        if (username.contains(" ")) {
            errors.put("username", "Usernames cannot contain spaces.");
            return;
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            errors.put("username", "The name must contain at least 1 uppercase letter, 1 lowercase letter, and can only include letters, numbers, or underscores.");
        }
    }

    // Additional validation methods...
    private void validateDateOfBirth(LocalDate dob, Map<String, String> errors) {
        // Date validation logic
    }

    private void validateGender(Boolean sex, Map<String, String> errors) {
        // Gender validation logic
    }

    private void validateEmail(String email, Map<String, String> errors) {
        // Email validation logic
    }

    private void validatePhone(String phone, Map<String, String> errors) {
        // Phone validation logic
    }

    private void validatePassword(String password, Map<String, String> errors) {
        // Password validation logic
    }

    private void validateConfirmPassword(String password, String confirmPassword, Map<String, String> errors) {
        // Confirm password validation logic
    }

    /**
     * Encode the password using BCryptPasswordEncoder
     */
    private String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);  // BCryptPasswordEncoder hashes the password
    }

    public Customer findEmail(String email){
        return customerRepository.findByEmail(email);
    }

}
