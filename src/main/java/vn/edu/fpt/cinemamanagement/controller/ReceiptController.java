package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.entities.*;
import vn.edu.fpt.cinemamanagement.repositories.BookingDetailRepository;
import vn.edu.fpt.cinemamanagement.repositories.ShowtimeSeatRepository;
import vn.edu.fpt.cinemamanagement.repositories.StaffRepository;
import vn.edu.fpt.cinemamanagement.services.BookingService;

import java.security.Principal;
import java.util.List;

@Controller
public class ReceiptController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingDetailRepository bookingDetailRepository;
    @Autowired
    private StaffRepository staffRepository;


    @GetMapping("/staffs/cashier/showtimes/receipt")
    public String showReceipt(Model model, Principal principal) {

        String username = principal.getName();
        Staff staff = staffRepository.findByUsername(username).orElse(null);
        model.addAttribute("staff", staff);
        Booking booking = bookingService.getReceipt();
        List<BookingDetail> bookingDetailList = bookingDetailRepository.findByBookingId(booking.getId());
        if(booking.getUserId().equalsIgnoreCase("KH000000")){
            model.addAttribute("walkInCustomer", "Walk-in Customer");
        }

        model.addAttribute("booking", booking);
        model.addAttribute("bookingDetails", bookingDetailList);

        return "booking/receipt";
    }
}

