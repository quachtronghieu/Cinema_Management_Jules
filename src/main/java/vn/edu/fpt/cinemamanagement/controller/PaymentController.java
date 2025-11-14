package vn.edu.fpt.cinemamanagement.controller;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.cinemamanagement.entities.*;
import vn.edu.fpt.cinemamanagement.repositories.*;
import vn.edu.fpt.cinemamanagement.services.BookingService;
import vn.edu.fpt.cinemamanagement.services.PaymentService;
import vn.edu.fpt.cinemamanagement.services.TicketService;

import java.security.Principal;
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
    @Autowired private PaymentService paymentService;
    @Autowired
    private BookingService bookingService;


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
        if(booking.getUserId().equalsIgnoreCase("KH000000")){
            ticket.setRedemptionStatus(true);
            ticket.setCheckedInTime(LocalDateTime.now());
            ticket.setRedemptionStaff(booking.getPayment().getStaff());
        } else {
            ticket.setRedemptionStatus(false);
        }
        ticketService.saveTicket(ticket);



        model.addAttribute("booking", booking);
        model.addAttribute("payment", payment);
        model.addAttribute("ticket", ticket);
        return "payment/payment_success";
    }

    @GetMapping("/ebanking/{bookingId}")
    public String customerEbanking(@PathVariable String bookingId, Model model) {
        Booking booking = bookingRepository.findById(bookingId);

        String paymentId = UUID.randomUUID().toString().substring(0, 8);

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setBooking(booking);
        payment.setPaymentMethod("E-Banking");
        payment.setPaymentStatus("Pending");
        payment.setPaymentTime(LocalDateTime.now());
        payment.setAmount(booking.getTotalAmount());

        paymentRepository.save(payment);
        String qrContent = "https://overtensely-communal-greta.ngrok-free.dev/payments/paymentsuccess?pay=" + paymentId;

        model.addAttribute("content", qrContent);
        return "/booking/payment";
    }
    @GetMapping("/receive-cash/{bookingId}/{staffId}")
    public String showReceiveCashPage(@PathVariable("bookingId") String bookingId,
                                      @PathVariable("staffId") String staffId,
                                      Model model,
                                      @RequestParam(value = "error", required = false) String errorMessage) {

        Booking booking = bookingService.findById(bookingId);
        if (booking == null) {
            return "redirect:/staffs/cashier/showtimes";
        }

        List<BookingDetail> bookingDetails = bookingDetailRepository.findByBookingId(bookingId);
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        model.addAttribute("booking", booking);
        model.addAttribute("bookingDetails", bookingDetails);
        model.addAttribute("staff", staff);
        model.addAttribute("walkInCustomer",
                "KH000000".equalsIgnoreCase(booking.getUserId()) ? "Walk-in Customer" : booking.getUserId());
        if (errorMessage != null) model.addAttribute("error", errorMessage);

        return "cashier/receive_cash_payment";
    }


    @PostMapping("/receive-cash/{bookingId}/{staffId}")
    @Transactional
    public String confirmCashPayment(@PathVariable("bookingId") String bookingId,
                                     @PathVariable("staffId") String staffId,
                                     @RequestParam("cashGiven") long cashGiven,
                                     RedirectAttributes redirectAttributes) {

        Booking booking = bookingService.findById(bookingId);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("error", "Booking not found!");
            return "redirect:/staffs/cashier/showtimes";
        }

        long total = booking.getTotalAmount().longValue();
        long change = cashGiven - total;
        if (cashGiven < total) {
            redirectAttributes.addAttribute("error", "Customer payment is less than total amount.");
            return "redirect:/payments/receive-cash/" + bookingId + "/" + staffId;
        }

        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        // Tạo Payment
        Payment payment = new Payment();
        payment.setId(paymentService.generatePaymentId());
        payment.setBooking(booking);
        payment.setPaymentMethod("CASH");
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentTime(LocalDateTime.now());
        payment.setPaymentStatus("PAID");
        payment.setStaff(staff);
        paymentRepository.save(payment);

        // Cập nhật Booking
        booking.setStatus("PAID");
        bookingRepository.save(booking);

        List<BookingDetail> details = bookingDetailRepository.findByBooking(booking);
        for (BookingDetail d : details) {
            ShowtimeSeat seat = d.getShowtimeSeat();
            if (seat != null) {  // chỉ cập nhật khi là ghế
                seat.setStatus("unavailable");
                showtimeSeatRepository.save(seat);
            }
        }

        // Tạo Ticket
        Ticket ticket = new Ticket();
        ticket.setId(ticketService.generateTicketId());
        ticket.setBooking(booking);
        ticket.setPrice(payment.getAmount());
        ticket.setRedemptionStatus(true);
        ticket.setCheckedInTime(LocalDateTime.now());
        ticket.setRedemptionStaff(staff);
        ticketService.saveTicket(ticket);

        redirectAttributes.addFlashAttribute("message",
                "Payment successful! Change to return: " + String.format("%,d VND", Math.max(0, change)));

        return "redirect:/staffs/cashier/showtimes";
    }

}
