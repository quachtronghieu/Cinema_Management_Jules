// src/main/java/vn/edu/fpt/cinemamanagement/dto/ShowtimeForm.java
package vn.edu.fpt.cinemamanagement.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class ShowtimeForm {
    private String showtimeId;
    private String movieId;
    private String roomId;
    private LocalDate showDate;
    private LocalTime startTime;


    public ShowtimeForm() {}

    public String getShowtimeId() { return showtimeId; }
    public void setShowtimeId(String showtimeId) { this.showtimeId = showtimeId; }
    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
}
