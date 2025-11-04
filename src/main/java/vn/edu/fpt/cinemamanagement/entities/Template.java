package vn.edu.fpt.cinemamanagement.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Template {
    @Id
    @Column(name = "template_id")
    private String id;
    private String name;
    private String description;

    @OneToOne(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private Room rooms;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TemplateSeat> templateSeats;
    public Template() {
    }

    public Template(String id, String name, String description, Room rooms) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.rooms = rooms;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Room getRooms() {
        return rooms;
    }

    public void setRooms(Room rooms) {
        this.rooms = rooms;
    }
}
