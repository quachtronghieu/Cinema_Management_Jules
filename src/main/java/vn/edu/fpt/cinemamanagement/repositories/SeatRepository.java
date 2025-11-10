package vn.edu.fpt.cinemamanagement.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.cinemamanagement.entities.TemplateSeat;

@Repository
public interface SeatRepository extends JpaRepository<TemplateSeat, String> {

}
