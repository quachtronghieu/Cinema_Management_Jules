package vn.edu.fpt.cinemamanagement.entities;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "Shift_Schedule")
public class ShiftSchedule {
    @Id
    @Column(name = "shift_schedule_id")
    private String shiftScheduleId;

    @ManyToOne
    @JoinColumn(name = "staff_id", referencedColumnName = "staff_id")
    private Staff staff;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "shift_date")
    private LocalDate shiftDate;

    @Column(name = "status")
    private String status;

    @Column(name ="shift_start")
    private LocalTime shiftStart;

    @Column(name = "late_minute")
    private Integer lateMinutes;

    public ShiftSchedule() {
    }

    public ShiftSchedule(String shiftScheduleId, Staff staff, LocalDate shiftDate, String status, LocalTime shiftStart, Integer lateMinutes) {
        this.shiftScheduleId = shiftScheduleId;
        this.staff = staff;
        this.shiftDate = shiftDate;
        this.status = status;
        this.shiftStart = shiftStart;
        this.lateMinutes = lateMinutes;
    }

    public LocalTime getShiftStart() {
        return shiftStart;
    }

    public void setShiftStart(LocalTime shiftStart) {
        this.shiftStart = shiftStart;
    }

    public Integer getLateMinutes() {
        return lateMinutes;
    }

    public void setLateMinutes(Integer lateMinutes) {
        this.lateMinutes = lateMinutes;
    }

    // Getters và Setters
    public String getShiftScheduleId() {
        return shiftScheduleId;
    }

    public void setShiftScheduleId(String shiftScheduleId) {
        this.shiftScheduleId = shiftScheduleId;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public void setShiftDate(LocalDate shiftDate) {
        this.shiftDate = shiftDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
