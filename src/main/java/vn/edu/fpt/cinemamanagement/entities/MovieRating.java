package vn.edu.fpt.cinemamanagement.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "MovieRating")
public class MovieRating {

    @Id
    @Column(name = "rating_id")
    private String ratingId;

    @Column(name = "movie_id")
    private String movieId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "rating_score")
    private int ratingScore;

    // ✅ Constructor mặc định bắt buộc cho JPA
    public MovieRating() {}

    // Constructor tiện lợi nếu muốn
    public MovieRating(String ratingId, String movieId, String userId, int ratingScore) {
        this.ratingId = ratingId;
        this.movieId = movieId;
        this.userId = userId;
        this.ratingScore = ratingScore;
    }

    // Getters & Setters
    public String getRatingId() { return ratingId; }
    public void setRatingId(String ratingId) { this.ratingId = ratingId; }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getRatingScore() { return ratingScore; }
    public void setRatingScore(int ratingScore) { this.ratingScore = ratingScore; }
}
