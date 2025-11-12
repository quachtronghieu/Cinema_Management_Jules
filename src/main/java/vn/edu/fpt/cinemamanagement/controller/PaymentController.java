package vn.edu.fpt.cinemamanagement.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.cinemamanagement.entities.*;
import vn.edu.fpt.cinemamanagement.repositories.*;
import vn.edu.fpt.cinemamanagement.services.TicketService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping(value = "/payments")
public class PaymentController {


    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    StaffRepository staffRepository;
    @Autowired
    PaymentRepository paymentRepository;
    @Autowired
    BookingDetailRepository bookingDetailRepository;
    @Autowired
    ShowtimeSeatRepository showtimeSeatRepository;
    @Autowired
    TicketService ticketService;



    @GetMapping("/ebanking/{bookingId}/{staffId}")
    public String ebanking(@PathVariable String bookingId, @PathVariable String staffId, Model model) {
        Booking booking = bookingRepository.findById(bookingId);

        String paymentId = UUID.randomUUID().toString().substring(0, 8);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setBooking(booking);
        payment.setPaymentMethod("E-Banking");
        payment.setPaymentStatus("Pending");
        payment.setPaymentTime(LocalDateTime.now());
        payment.setAmount(booking.getTotalAmount());
        payment.setStaff(staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found")));

        paymentRepository.save(payment);
        String qrContent = "https://unquivering-latrice-semisentimental.ngrok-free.dev/payments/paymentsuccess?pay=" + paymentId;

        model.addAttribute("content", qrContent);
        return "/payment/QR_payment";
    }

    @GetMapping("/paymentsuccess")
    public String paymentSuccessPage(@RequestParam("pay") String paymentId, Model model) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        // Cập nhật payment
        payment.setPaymentStatus("success");
        payment.setAmount(payment.getBooking().getTotalAmount());
        paymentRepository.save(payment);

        // Cập nhật booking
        Booking booking = payment.getBooking();
        booking.setStatus("paid");
        bookingRepository.save(booking);

        // Cập nhật trạng thái ghế
        List<BookingDetail> details = bookingDetailRepository.findByBooking(booking);
        for (BookingDetail d : details) {
            ShowtimeSeat seat = d.getShowtimeSeat();
            if (seat != null) {  // chỉ cập nhật khi là ghế
                seat.setStatus("unavailable");
                showtimeSeatRepository.save(seat);
            }
        }

        Ticket ticket = new Ticket();
        ticket.setId(ticketService.generateTicketId());
        ticket.setBooking(booking);
        ticket.setPrice(payment.getAmount());
        ticket.setRedemptionStatus(true);
        ticket.setCheckedInTime(LocalDateTime.now());
        ticketService.saveTicket(ticket);



        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);
        model.addAttribute("ticket", ticket);
        return "payment/payment_success";
    }
}
