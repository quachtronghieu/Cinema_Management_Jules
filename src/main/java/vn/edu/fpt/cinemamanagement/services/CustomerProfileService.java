package vn.edu.fpt.cinemamanagement.services;

import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.repositories.CustomerRepository;

@Service
public class CustomerProfileService {

    private final CustomerRepository customerRepository;

    public CustomerProfileService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer getCustomerByUsername(String username) {
        return customerRepository.findByUsername(username).orElse(null);
    }

    public boolean validateCustomer(Model model, Customer customer) {
        boolean valid = true;

        if (customer.getUsername().isEmpty()) {
            model.addAttribute("usernameError", "Username cannot be empty");
            valid = false;
        }

        if (!customer.getEmail().matches(".+@.+\\..+")) {
            model.addAttribute("emailError", "Invalid email");
            valid = false;
        }

        if (!customer.getPhone().matches("\\d{10,12}")) {
            model.addAttribute("phoneError", "Invalid phone number");
            valid = false;
        }

        return valid;
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }


}
