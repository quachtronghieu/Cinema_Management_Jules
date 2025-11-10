package vn.edu.fpt.cinemamanagement.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.cinemamanagement.entities.ShiftSchedule;
import vn.edu.fpt.cinemamanagement.entities.Staff;

import java.time.LocalDate;
import java.util.List;

public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, String> {
    ShiftSchedule findTopByOrderByShiftScheduleIdDesc();
    List<ShiftSchedule> findByShiftDate(LocalDate date);
    List<ShiftSchedule> findByStaff(Staff staff);
    @Query("SELECT s FROM ShiftSchedule s WHERE s.staff.staffID = :staffID ORDER BY s.shiftDate ASC")
    List<ShiftSchedule> findByStaffID(@Param("staffID") String staffID);

    List<ShiftSchedule> findByStaffAndShiftDate(Staff staff, LocalDate shiftDate);


}
