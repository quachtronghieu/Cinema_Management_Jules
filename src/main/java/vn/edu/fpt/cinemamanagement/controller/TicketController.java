package vn.edu.fpt.cinemamanagement.controller;

import org.hibernate.annotations.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.fpt.cinemamanagement.entities.Booking;
import vn.edu.fpt.cinemamanagement.entities.BookingDetail;
import vn.edu.fpt.cinemamanagement.entities.Ticket;
import vn.edu.fpt.cinemamanagement.repositories.TicketRepository;
import vn.edu.fpt.cinemamanagement.services.BookingService;
import vn.edu.fpt.cinemamanagement.services.TicketService;

import java.util.List;

@Controller
public class TicketController {

    @Autowired
    private TicketService ticketService;
    @Autowired
    private BookingService bookingService;

    @RequestMapping("ticket/{bookingId}")
    public String ticket(Model model, @PathVariable String bookingId) {
        Ticket ticket =  ticketService.findTicketByBookingId(bookingId);
        Booking booking = bookingService.finBookingById(bookingId);
        List<BookingDetail> details = bookingService.getBookingDetail(bookingId);
        model.addAttribute("booking",booking);
        model.addAttribute("tickets", ticket);
        model.addAttribute("details", details);
        return "tickets/ticket";
    }
}
