package vn.edu.fpt.cinemamanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.cinemamanagement.entities.Ticket;
import vn.edu.fpt.cinemamanagement.repositories.TicketRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;

    public String generateTicketId() {
        String lastId = ticketRepository.findMaxTicketId(); // ví dụ: "TK000123"

        int nextNumber = 1; // mặc định nếu chưa có ticket nào
        if (lastId != null) {
            nextNumber = Integer.parseInt(lastId.substring(2)) + 1;
        }

        return String.format("TK%06d", nextNumber); // TK000124
    }

    public Ticket findTicketByBookingId(String bookingId) {
        return ticketRepository.findByBookingId(bookingId);
    }

    public Ticket saveTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Page<Ticket> findAllTickets(Pageable pageable){
        return ticketRepository.findAll(pageable);
    }

    public Ticket findTicketByBookingId(String bookingId) {
        return ticketRepository.findByBookingId(bookingId);
    }



}
