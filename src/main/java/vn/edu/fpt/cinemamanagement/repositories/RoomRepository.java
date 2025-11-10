package vn.edu.fpt.cinemamanagement.repositories;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.cinemamanagement.entities.Room;
import vn.edu.fpt.cinemamanagement.entities.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, String> {
    List<Room> findByStatus(String status);

    List<Room> findById( Template template);


}
