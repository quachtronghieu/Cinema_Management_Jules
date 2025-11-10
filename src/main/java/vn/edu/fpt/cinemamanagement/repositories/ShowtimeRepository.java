package vn.edu.fpt.cinemamanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.cinemamanagement.entities.Movie;
import vn.edu.fpt.cinemamanagement.entities.Room;
import vn.edu.fpt.cinemamanagement.entities.Showtime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ShowtimeRepository extends JpaRepository<Showtime, String> {

    List<Showtime> findByShowDate(LocalDate date);
    List<Showtime> findByRoom_IdAndShowDate(String roomId, LocalDate date);
    Optional<Showtime> findTopByOrderByShowtimeIdDesc();
    Showtime findByShowtimeId(String showtimeId);
    boolean existsByMovieAndRoomAndShowDateAndStartTime(
            Movie movie,
            Room room,
            LocalDate showDate,
            LocalTime startTime
    );

    /**
     * 🔹 Kiểm tra trùng giờ trong cùng phòng (phòng + ngày + khoảng giờ)
     */
    @Query(value = """
        SELECT CASE WHEN COUNT(*) > 0 THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END
        FROM showtime s
        WHERE s.room_id = :roomId
          AND s.show_date = :date
          AND CAST(s.start_time AS time) < CAST(:end AS time)
          AND CAST(:start AS time) < CAST(s.end_time AS time)
    """, nativeQuery = true)
    boolean hasOverlapInRoom(
            @Param("roomId") String roomId,
            @Param("date") LocalDate date,
            @Param("start") LocalTime start,
            @Param("end") LocalTime end
    );

    /**
     * 🔹 Kiểm tra cùng phim chiếu ở phòng khác cùng khung giờ
     * (cấm 2 phòng chiếu cùng 1 phim cùng thời gian)
     */
    @Query(value = """
        SELECT CASE WHEN COUNT(*) > 0 THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END
        FROM showtime s
        WHERE s.movie_id = :movieId
          AND s.show_date = :date
          AND s.room_id <> :roomId
          AND CAST(s.start_time AS time) < CAST(:end AS time)
          AND CAST(:start AS time) < CAST(s.end_time AS time)
    """, nativeQuery = true)
    boolean hasSameMovieInOtherRoom(
            @Param("movieId") String movieId,
            @Param("roomId") String roomId,
            @Param("date") LocalDate date,
            @Param("start") LocalTime start,
            @Param("end") LocalTime end
    );

    @Query(value = """
    SELECT CASE WHEN COUNT(*) > 0 THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) END
    FROM showtime s
    WHERE s.room_id = :roomId
      AND s.show_date = :date
      AND CAST(s.start_time AS time) < CAST(:end AS time)
      AND CAST(:start AS time) < CAST(s.end_time AS time)
""", nativeQuery = true)
    boolean existsOverlap(
            @Param("roomId") String roomId,
            @Param("date") LocalDate date,
            @Param("start") LocalTime start,
            @Param("end") LocalTime end
    );

    List<Showtime> findAllByMovie_MovieIDAndShowDate(String movieId, LocalDate selectedDate);

    @Query(value = "SELECT TOP 1 * FROM showtime " +
            "WHERE movie_id = :movieId " +
            "AND show_date = :date " +
            "AND CONVERT(time, start_time) = CONVERT(time, :time)",
            nativeQuery = true)
    Showtime findMovieAndDateTime(@Param("movieId") String movieId,
                                  @Param("date") LocalDate date,
                                  @Param("time") LocalTime time);
}
