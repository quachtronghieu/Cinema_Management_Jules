package vn.edu.fpt.cinemamanagement.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "Showtime_Seat")
public class ShowtimeSeat {

    @Id
    @Column(name = "showtime_seat_id", length = 8)
    private String showtimeSeatID;

    @ManyToOne
    @JoinColumn(name = "showtime_id", referencedColumnName = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne
    @JoinColumn(name = "template_seat_id", referencedColumnName = "template_seat_id", nullable = false)
    private TemplateSeat templateSeat;

    private String status;

    public ShowtimeSeat() {
    }

    public ShowtimeSeat(String showtimeSeatID, Showtime showtime, TemplateSeat templateSeat, String status) {
        this.showtimeSeatID = showtimeSeatID;
        this.showtime = showtime;
        this.templateSeat = templateSeat;
        this.status = status;
    }

    public String getShowtimeSeatID() {
        return showtimeSeatID;
    }

    public void setShowtimeSeatID(String showtimeSeatID) {
        this.showtimeSeatID = showtimeSeatID;
    }

    public Showtime getShowtime() {
        return showtime;
    }

    public void setShowtime(Showtime showtime) {
        this.showtime = showtime;
    }

    public TemplateSeat getTemplateSeat() {
        return templateSeat;
    }

    public void setTemplateSeat(TemplateSeat templateSeat) {
        this.templateSeat = templateSeat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
