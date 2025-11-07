package vn.edu.fpt.cinemamanagement.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "template_seat")
public class TemplateSeat {
    @Id
    @Column(name = "template_seat_id")
    private String id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", referencedColumnName = "template_id")
    private Template template;
    @Column(name = "row_label")
    private String rowLabel;
    @Column(name = "seat_number")
    private int seatNumber;
    @Column(name = "seat_type")
    private String seatType;

    public TemplateSeat() {
    }

    public TemplateSeat(String id, Template template, String rowLabel, int seatNumber, String seatType) {
        this.id = id;
        this.template = template;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
        this.seatType = seatType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public String getRowLabel() {
        return rowLabel;
    }

    public void setRowLabel(String rowLabel) {
        this.rowLabel = rowLabel;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatType() {
        return seatType;
    }

    public void setSeatType(String seatType) {
        this.seatType = seatType;
    }
}
