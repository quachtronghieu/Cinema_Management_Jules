package vn.edu.fpt.cinemamanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cinemamanagement.entities.Ticket;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,String> {

    @Query("SELECT MAX(t.id) FROM Ticket t")
    String findMaxTicketId();

    Ticket findByBookingId(String bookingId);

}
