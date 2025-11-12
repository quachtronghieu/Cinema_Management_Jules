package vn.edu.fpt.cinemamanagement.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Ticket {
    @Id
    @Column(name = "ticket_id")
    private String id;

    @OneToOne
    @JoinColumn(name = "booking_id", referencedColumnName = "booking_id")
    private Booking booking;
    private BigDecimal price;
    @Column(name = "checked_in")
    private boolean redemptionStatus;
    @Column(name = "checked_in_time")
    private LocalDateTime checkedInTime;
    @ManyToOne
    @JoinColumn(name = "checked_in_staff_id")
    private Staff redemptionStaff;

    public Ticket() {
    }


    public Ticket(String id, Booking booking, BigDecimal price, boolean redemptionStatus, LocalDateTime checkedInTime, Staff redemptionStaff, Customer customer) {
        this.id = id;
        this.booking = booking;
        this.price = price;
        this.redemptionStatus = redemptionStatus;
        this.checkedInTime = checkedInTime;
        this.redemptionStaff = redemptionStaff;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isRedemptionStatus() {
        return redemptionStatus;
    }

    public void setRedemptionStatus(boolean redemptionStatus) {
        this.redemptionStatus = redemptionStatus;
    }

    public LocalDateTime getCheckedInTime() {
        return checkedInTime;
    }

    public void setCheckedInTime(LocalDateTime checkedInTime) {
        this.checkedInTime = checkedInTime;
    }

    public Staff getRedemptionStaff() {
        return redemptionStaff;
    }

    public void setRedemptionStaff(Staff redemptionStaff) {
        this.redemptionStaff = redemptionStaff;
    }
}
