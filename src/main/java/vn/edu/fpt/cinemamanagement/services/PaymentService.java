package vn.edu.fpt.cinemamanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cinemamanagement.repositories.PaymentRepository;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    public String generatePaymentId() {
        String prefix = "PM";
        long count = paymentRepository.count() + 1;
        return prefix + String.format("%06d", count);
    }
}
