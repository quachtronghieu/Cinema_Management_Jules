package vn.edu.fpt.cinemamanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.repositories.CustomerRepository;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Injecting PasswordEncoder

    // Register customer method with validation
    public Map<String, String> registerCustomer(Customer customer, String confirmPassword) {
        Map<String, String> errors = new HashMap<>();

        // Validate customer details (username, email, etc.)
        if (customerRepository.existsByUsername(customer.getUsername())) {
            errors.put("username", "Username already exists");
        }
        if (customerRepository.existsByEmail(customer.getEmail())) {
            errors.put("email", "Email already exists");
        }

        // Check if passwords match
        if (!customer.getPassword().equals(confirmPassword)) {
            errors.put("confirmPassword", "Passwords do not match");
        }

        // Encrypt password before saving to the database
        if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));  // Hash the password
        }

        // Save the customer
        if (errors.isEmpty()) {
            customerRepository.save(customer);
        }

        return errors;
    }

    // Login method to authenticate the user manually (without AuthenticationManager)
    public boolean login(String username, String password) {
        // Retrieve the customer based on username
        Customer customer = customerRepository.findByUsername(username).orElse(null);

        if (customer != null) {
            // Check if the password matches (using PasswordEncoder for hashed passwords)
            return passwordEncoder.matches(password, customer.getPassword());
        }

        // If no customer found or password doesn't match, return false
        return false;
    }
}
