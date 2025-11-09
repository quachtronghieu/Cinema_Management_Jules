package vn.edu.fpt.cinemamanagement.repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cinemamanagement.entities.ShowtimeSeat;

import java.util.List;

@Repository
public interface ShowtimeSeatRepository extends JpaRepository<ShowtimeSeat, String> {

    @Query(value = "SELECT TOP 1 showtime_seat_id FROM Showtime_Seat ORDER BY showtime_seat_id DESC", nativeQuery = true)
    String findLastId();

    List<ShowtimeSeat> getAllByShowtime_ShowtimeId(String showtimeShowtimeId);
}
