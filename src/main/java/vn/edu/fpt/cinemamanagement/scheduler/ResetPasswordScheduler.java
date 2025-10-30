package vn.edu.fpt.cinemamanagement.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.repositories.CustomerRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ResetPasswordScheduler {
    private CustomerRepository customerRepository;

    public ResetPasswordScheduler(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Scheduled(fixedRate = 120000) //Scan per 2'
    public void scheduledResetPassword() {
        LocalDateTime now = LocalDateTime.now();

        List<Customer> expired = customerRepository.findByVerifyAndResetRequestedAtBefore("resetPassword", now.minusMinutes(10));

        for (Customer customer : expired) {
            customer.setVerify("active");
            customer.setResetRequestedAt(null);
        }

        if (!expired.isEmpty()){
            customerRepository.saveAll(expired);
        }
    }
}
