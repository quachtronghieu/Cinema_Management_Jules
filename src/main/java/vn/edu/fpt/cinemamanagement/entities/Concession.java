package vn.edu.fpt.cinemamanagement.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Concession")
public class Concession {

    @Id
    @Column(name = "concession_id", length = 8, nullable = false)
    private String concessionId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "description", length = 255, nullable = false)
    private String description;

    @Column(name = "img", length = 255, nullable = false)
    private String img;

    public Concession() {}

    public String getConcessionId() { return concessionId; }
    public void setConcessionId(String concessionId) { this.concessionId = concessionId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImg() { return img; }
    public void setImg(String img) { this.img = img; }
}
