package vn.edu.fpt.cinemamanagement.services;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.entities.Staff;
import vn.edu.fpt.cinemamanagement.repositories.CustomerRepository;
import vn.edu.fpt.cinemamanagement.repositories.StaffRepository;

import java.util.Collections;

@Service
@Transactional
public class CustomUserDetailsService implements UserDetailsService {

    private final StaffRepository staffRepository;
    private final CustomerRepository customerRepository;

    public CustomUserDetailsService(StaffRepository staffRepository, CustomerRepository customerRepository) {
        this.staffRepository = staffRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        // Kiểm tra username có ký tự viết hoa không
        boolean hasUpperCase = !username.equals(username.toLowerCase());

        if (hasUpperCase) {
            // Là Customer
            Customer customer = customerRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            return User.builder()
                    .username(customer.getUsername())
                    .password(customer.getPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                    .build();
        } else {
            // Là Staff
            Staff staff = staffRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Staff not found"));
            String role = "ROLE_" + staff.getPosition().toUpperCase();
            return User.builder()
                    .username(staff.getUsername())
                    .password(staff.getPassword())
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                    .build();
        }
    }
}
