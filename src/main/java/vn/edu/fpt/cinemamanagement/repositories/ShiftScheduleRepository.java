package vn.edu.fpt.cinemamanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.cinemamanagement.entities.ShiftSchedule;

import java.time.LocalDate;
import java.util.List;

public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, String> {
    ShiftSchedule findTopByOrderByShiftScheduleIdDesc();
    List<ShiftSchedule> findByShiftDate(LocalDate date);
}
