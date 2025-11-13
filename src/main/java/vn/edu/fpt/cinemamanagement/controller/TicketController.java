package vn.edu.fpt.cinemamanagement.controller;

import org.hibernate.annotations.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.cinemamanagement.entities.Booking;
import vn.edu.fpt.cinemamanagement.entities.BookingDetail;
import vn.edu.fpt.cinemamanagement.entities.Ticket;
import vn.edu.fpt.cinemamanagement.repositories.TicketRepository;
import vn.edu.fpt.cinemamanagement.repositories.BookingDetailRepository;
import vn.edu.fpt.cinemamanagement.services.BookingService;
import vn.edu.fpt.cinemamanagement.services.TicketService;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingDetailRepository bookingDetailRepository;

    @GetMapping
    public String listTicket(Model model,
                             @RequestParam(name = "page", defaultValue = "1") int page,
                             Principal principal) { // 👈 thêm principal vào

        int size = 5;
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Ticket> ticketPage = ticketService.findAllTickets(pageable, principal);

        model.addAttribute("tickets", ticketPage.getContent());

        int totalPages = ticketPage.getTotalPages();
        int currentPage = page;

        int visiblePages = 5;
        int startPage = 1;
        int endPage = totalPages > 0 ? Math.min(visiblePages, totalPages) : 1;

        if (totalPages > visiblePages) {
            startPage = ((currentPage - 1) / visiblePages) * visiblePages + 1;
            endPage = Math.min(startPage + visiblePages - 1, totalPages);
        }

        model.addAttribute("ticketPage", ticketPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);
        System.out.println(">>> principal.getName() = " + principal.getName());

        return "customers/my_booking_history";
    }


    @GetMapping("/{bookingId}")
    public String ticket(Model model, @PathVariable String bookingId) {
        Ticket ticket =  ticketService.findTicketByBookingId(bookingId);
        Booking booking = bookingService.findBookingById(bookingId);
        List<BookingDetail> details = bookingService.getBookingDetail(bookingId);
        model.addAttribute("booking",booking);
        model.addAttribute("details",details);
        model.addAttribute("ticket",ticket);
        return "tickets/ticket";
    }



    @GetMapping("/ticket_detail/{bookingId}")
    public String detailTicket(@PathVariable("bookingId") String bookingId, Model model) {
        Booking booking = bookingService.findById(bookingId);
        if (booking == null) return "redirect:/ticket";

        List<BookingDetail> bookingDetails = bookingDetailRepository.findByBookingId(booking.getId());
        Ticket ticket = ticketService.findTicketByBookingId(bookingId);

        model.addAttribute("booking", booking);
        model.addAttribute("bookingDetails", bookingDetails);
        model.addAttribute("tickets", ticket);
        return "customers/detail_ticket";
    }



}
