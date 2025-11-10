package vn.edu.fpt.cinemamanagement.repositories;

import vn.edu.fpt.cinemamanagement.entities.TemplateSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface TemplateSeatRepository extends JpaRepository<TemplateSeat, String> {
    List<TemplateSeat> findByTemplate_Id(String templateID);
    long countByTemplate_Id(String templateID);
    TemplateSeat findByRowLabelAndSeatNumber(String rowLabel, int seatNumber);


}
