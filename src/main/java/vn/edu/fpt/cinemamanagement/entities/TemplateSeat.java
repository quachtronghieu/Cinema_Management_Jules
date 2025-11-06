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
    private String row_label;
    private int seat_number;
    private String seat_type;

    public TemplateSeat() {
    }

    public TemplateSeat(String seat_type, int seat_number, String row_label, Template template, String id) {
        this.seat_type = seat_type;
        this.seat_number = seat_number;
        this.row_label = row_label;
        this.template = template;
        this.id = id;
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

    public String getRow_label() {
        return row_label;
    }

    public void setRow_label(String row_label) {
        this.row_label = row_label;
    }

    public int getSeat_number() {
        return seat_number;
    }

    public void setSeat_number(int seat_number) {
        this.seat_number = seat_number;
    }

    public String getSeat_type() {
        return seat_type;
    }

    public void setSeat_type(String seat_type) {
        this.seat_type = seat_type;
    }
}
