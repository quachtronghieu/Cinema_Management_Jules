package vn.edu.fpt.cinemamanagement.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "room")
public class Room {
    @Id
    @Column(name = "room_id")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", referencedColumnName = "template_id")
    private Template template;

    @Column(name = "status")
    private String status;

    public Room() {
    }

    public Room(String id, Template template, String status) {
        this.id = id;
        this.template = template;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
