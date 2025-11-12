package vn.edu.fpt.cinemamanagement.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Booking_Detail")
public class BookingDetail {
    @Id
    @Column(name = "booking_detail_id", length = 8)
    private String bookingDetailId;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "item_type", length = 20)
    private String itemType; // "seat" hoặc "concession"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "showtime_seat_id", nullable = true)
    private ShowtimeSeat showtimeSeat;

    @ManyToOne
    @JoinColumn(name = "concession_id", nullable = true)
    private Concession concession;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;

    // total_price là computed column => không map vào DB
    @Transient
    private BigDecimal totalPrice;

    public BookingDetail() {
    }

    public BookingDetail(String bookingDetailId, Booking booking, String itemType, ShowtimeSeat showtimeSeat, Concession concession, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.bookingDetailId = bookingDetailId;
        this.booking = booking;
        this.itemType = itemType;
        this.showtimeSeat = showtimeSeat;
        this.concession = concession;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public String getBookingDetailId() {
        return bookingDetailId;
    }

    public void setBookingDetailId(String bookingDetailId) {
        this.bookingDetailId = bookingDetailId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public ShowtimeSeat getShowtimeSeat() {
        return showtimeSeat;
    }

    public void setShowtimeSeat(ShowtimeSeat showtimeSeat) {
        this.showtimeSeat = showtimeSeat;
    }

    public Concession getConcession() {
        return concession;
    }

    public void setConcession(Concession concession) {
        this.concession = concession;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
