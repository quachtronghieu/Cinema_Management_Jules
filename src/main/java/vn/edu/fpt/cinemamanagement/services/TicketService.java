package vn.edu.fpt.cinemamanagement.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.cinemamanagement.entities.Customer;
import vn.edu.fpt.cinemamanagement.entities.Ticket;
import vn.edu.fpt.cinemamanagement.repositories.BookingRepository;
import vn.edu.fpt.cinemamanagement.repositories.CustomerRepository;
import vn.edu.fpt.cinemamanagement.entities.*;
import vn.edu.fpt.cinemamanagement.repositories.TicketRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    TicketRepository ticketRepository;
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    private CustomerRepository customerRepository;

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
    public Page<Ticket> findAllTickets(Pageable pageable, Principal principal) {
        String username = principal.getName();
        Optional<Customer> customerOpt = customerRepository.findByUsername(username);
        Customer customer = customerOpt.get();
        return ticketRepository.findByBooking_UserId(customer.getUser_id(), pageable);
    }
    public List<Map<String, Object>> displayData() {
        // 🔹 Chỉ lấy vé chưa check-in (đặt online)
        List<Ticket> tickets = ticketRepository.findByRedemptionStatusFalse();
        List<Map<String, Object>> displayList = new ArrayList<>();

        for (Ticket t : tickets) {
            Map<String, Object> row = new HashMap<>();

            // ====== THÔNG TIN CƠ BẢN ======
            row.put("ticketId", t.getId());
            row.put("bookingId", t.getBooking() != null ? t.getBooking().getId() : "N/A");
            row.put("checkedIn", t.isRedemptionStatus());

            // ====== MOVIE / SHOWTIME / ROOM / SEAT ======
            String movieTitle = "N/A";
            String showtimeHour = "-";
            String roomSeat = "-";

            if (t.getBooking() != null && t.getBooking().getBookingDetails() != null && !t.getBooking().getBookingDetails().isEmpty()) {
                List<BookingDetail> details = t.getBooking().getBookingDetails();
                BookingDetail firstDetail = details.get(0);

                if (firstDetail.getShowtimeSeat() != null && firstDetail.getShowtimeSeat().getShowtime() != null) {
                    Showtime st = firstDetail.getShowtimeSeat().getShowtime();

                    //  Movie title
                    if (st.getMovie() != null)
                        movieTitle = st.getMovie().getTitle();

                    // Chỉ hiển thị giờ chiếu
                    if (st.getStartTime() != null)
                        showtimeHour = st.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));

                    //  Room +  Gộp các ghế
                    String roomName = (st.getRoom() != null) ? st.getRoom().getId() : "N/A";
                    List<String> seatLabels = new ArrayList<>();

                    for (BookingDetail bd : details) {
                        if (bd.getShowtimeSeat() != null && bd.getShowtimeSeat().getTemplateSeat() != null) {
                            TemplateSeat ts = bd.getShowtimeSeat().getTemplateSeat();
                            seatLabels.add(ts.getRowLabel() + ts.getSeatNumber());
                        }
                    }

                    String seatJoined = seatLabels.isEmpty() ? "-" : String.join(", ", seatLabels);
                    roomSeat = roomName + " / " + seatJoined;
                }
            }

            row.put("movieTitle", movieTitle);
            row.put("showtime", showtimeHour);
            row.put("roomSeat", roomSeat);

            displayList.add(row);
        }

        return displayList;
    }


    @Transactional
    public String Checkin_Ticket(String ticketId, Staff staff) {
        Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
        if (ticket != null) {
            ticket.setRedemptionStatus(true);
            ticket.setCheckedInTime(LocalDateTime.now());
            ticket.setRedemptionStaff(staff);
            return null;
        } else return "Ticket not found";
    }



}
