package vn.edu.fpt.cinemamanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cinemamanagement.entities.Movie;
import vn.edu.fpt.cinemamanagement.entities.MovieRating;
import java.util.List;
import vn.edu.fpt.cinemamanagement.repositories.MovieRatingRepository;

@Repository
public interface MovieRatingRepository extends JpaRepository<MovieRating, String> {

    @Query("SELECT mr.movieId, AVG(mr.ratingScore) as avgScore " +
            "FROM MovieRating mr " +
            "GROUP BY mr.movieId " +
            "ORDER BY avgScore DESC")
    List<Object[]> findTop5RatedMovies();

    List<MovieRating> findByMovieId(String movieId);
    boolean existsByMovieIdAndUserId(String movieId, String userId);
}
