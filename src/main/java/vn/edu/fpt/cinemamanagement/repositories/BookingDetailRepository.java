package vn.edu.fpt.cinemamanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cinemamanagement.entities.Booking;
import vn.edu.fpt.cinemamanagement.entities.BookingDetail;

import java.util.List;

@Repository
public interface BookingDetailRepository extends JpaRepository<BookingDetail, Integer> {
    BookingDetail findTopByOrderByBookingDetailIdDesc();
    List<BookingDetail> findByBookingId(String bookingId);

    List<BookingDetail> findByBooking(Booking booking);

}
