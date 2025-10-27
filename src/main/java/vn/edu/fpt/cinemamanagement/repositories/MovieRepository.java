package vn.edu.fpt.cinemamanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cinemamanagement.entities.Movie;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {
    Movie findByMovieID(String movieID);
    @Query(value = "select top 1 movie_id from Movie order by movie_id desc",  nativeQuery = true)
            String findLastMovieId();
    boolean existsByMovieID(String movieID);
    boolean existsByTitle(String title);
    // Huynh Anh - Added to filter currently showing movies
    List<Movie> findByReleaseDateLessThanEqual(LocalDate date);

    boolean existsByTitleIgnoreCase(String title);

    Page<Movie> findByReleaseDateGreaterThan(LocalDate date, Pageable pageable);
}
