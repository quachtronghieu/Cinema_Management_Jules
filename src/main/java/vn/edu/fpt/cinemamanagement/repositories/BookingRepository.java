package vn.edu.fpt.cinemamanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cinemamanagement.entities.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {
    Booking findTopByOrderByIdDesc();
}
