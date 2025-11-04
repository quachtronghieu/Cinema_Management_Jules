package vn.edu.fpt.cinemamanagement.services;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.repositories.CustomerRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class CustomerProfileService {

    private final CustomerRepository customerRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[0-9]{9,11}$"
    );


    public CustomerProfileService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username).orElse(null);
    }


    public Map<String, String> validateCustomer(Customer customer) {
        Map<String, String> errors = new HashMap<>();

        // 1. VALIDATE FORMAT
        validateDateOfBirth(customer.getDob(), errors);
        validateEmail(customer.getEmail(), errors);
        validatePhone(customer.getPhone(), errors);

        // 2. VALIDATE BUSINESS RULES - check trùng lặp trong DB

        if (customerRepository.existsByEmail(customer.getEmail()) && !customer.getUser_id().equals(customer.getUser_id())) {
            errors.put("email", "Email already exists");
        }

        if (customerRepository.existsByPhone(customer.getPhone()) && !customer.getUser_id().equals(customer.getUser_id())) {
            errors.put("phone", "Phone already exists");
        }



        return errors; // trả về map lỗi, empty nếu hợp lệ
    }

    /**
     * Validate date of birth
     */
    private void validateDateOfBirth(LocalDate dob, Map<String, String> errors) {
        if (dob == null) {
            errors.put("dob", "Date of birth is required.");
            return;
        }

        LocalDate today = LocalDate.now();
        if (dob.isAfter(today)) {
            errors.put("dob", "The date of birth cannot be later than today.");
            return;
        }

        int year = dob.getYear();
        int currentYear = LocalDate.now().getYear();
        if (year < 1900 || year > currentYear) {
            errors.put("dob", String.format("The year of birth must be between 1900–%d", currentYear));
        }

        int birthYear = dob.getYear();
        int age = currentYear - birthYear;
        if (age < 10) {
            errors.put("dob", "You must be at least 10 years old.");
        }
    }

    /**
     * Validate email
     */
    private void validateEmail(String email, Map<String, String> errors) {
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", "Email is required.");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.put("email", "Invalid email.");
        }
    }

    /**
     * Validate phone
     */
    private void validatePhone(String phone, Map<String, String> errors) {
        if (phone == null || phone.trim().isEmpty()) {
            errors.put("phone", "The phone number is required.");
            return;
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            errors.put("phone", "The phone number must have 9-11 digits.");
        }
    }


    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }


}
