package vn.edu.fpt.cinemamanagement.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Payment {
    @Id
    @Column(name = "payment_id")
    private String id;

    @OneToOne
    @JoinColumn(name = "booking_id", referencedColumnName = "booking_id")
    private Booking booking ;

    @Column(name = "payment_method")
    private String paymentMethod ;
    @Column(name = "payment_time")
    private LocalDateTime paymentTime ;
    private BigDecimal amount;
    @Column(name = "payment_status")
    private String paymentStatus ;
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private  Staff staff;

    public Payment() {}

    public Payment(String id, Booking booking, String paymentMethod, LocalDateTime paymentTime, BigDecimal amount, String paymentStatus, Staff staff) {
        this.id = id;
        this.booking = booking;
        this.paymentMethod = paymentMethod;
        this.paymentTime = paymentTime;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.staff = staff;
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

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(LocalDateTime paymentTime) {
        this.paymentTime = paymentTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }
}
