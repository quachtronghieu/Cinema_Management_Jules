package vn.edu.fpt.cinemamanagement.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cinemamanagement.entities.ShiftSchedule;
import vn.edu.fpt.cinemamanagement.repositories.ShiftScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ShiftScheduleService {

    @Autowired
    private ShiftScheduleRepository shiftScheduleRepository;

    // --- Lấy tất cả lịch làm việc (phân trang) ---
    public Page<ShiftSchedule> findAllSchedules(Pageable pageable) {
        return shiftScheduleRepository.findAll(pageable);
    }

    // --- Lấy theo ID ---
    public Optional<ShiftSchedule> getShiftScheduleById(String id) {
        return shiftScheduleRepository.findById(id);
    }

    public void validateShiftScheduleDate(ShiftSchedule shiftSchedule) {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(7);

        LocalDate shiftDate = shiftSchedule.getShiftDate();

        if (shiftDate.isBefore(today)) {
            throw new IllegalArgumentException("Shift date cannot be on the past.");
        }
        if (shiftDate.isAfter(maxDate)) {
            throw new IllegalArgumentException("Shift date cannot be more than 7 days from today.");
        }
    }





    // --- Tạo mới ---
    public void createShiftSchedule(ShiftSchedule shiftSchedule) {
        validateShiftScheduleDate(shiftSchedule); // ✅ gọi kiểm tra trước khi lưu

        if (shiftSchedule.getShiftScheduleId() == null || shiftSchedule.getShiftScheduleId().isEmpty()) {
            shiftSchedule.setShiftScheduleId(generateNewShiftScheduleID());
        }
        shiftScheduleRepository.save(shiftSchedule);
    }

    // --- Lưu hoặc cập nhật ---
    public ShiftSchedule save(ShiftSchedule schedule) {
        validateShiftScheduleDate(schedule); // ✅ gọi kiểm tra trước khi lưu

        if (schedule.getShiftScheduleId() == null || schedule.getShiftScheduleId().isEmpty()) {
            schedule.setShiftScheduleId(generateNewShiftScheduleID());
        }
        return shiftScheduleRepository.save(schedule);
    }

    // --- Generate ID tự động ---
    private String generateNewShiftScheduleID() {
        ShiftSchedule lastShift = shiftScheduleRepository.findTopByOrderByShiftScheduleIdDesc();
        if (lastShift == null) {
            return "SC0001";
        }

        String lastID = lastShift.getShiftScheduleId(); // Ex: SC0042
        int number = Integer.parseInt(lastID.substring(2)) + 1; // 43
        return String.format("SC%04d", number); // => SC0043
    }

    public void deleteShiftSchedule(String id) {
        if (shiftScheduleRepository.existsById(id)) {
            shiftScheduleRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Shift Schedule ID not found: " + id);
        }
    }

    public void updateShiftStatus(ShiftSchedule shiftSchedule, LocalDateTime checkInTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime shiftStartTime = LocalDateTime.of(shiftSchedule.getShiftDate(), shiftSchedule.getShiftStart());

        // local variable
        boolean isCheckIn = checkInTime != null;

        if (!isCheckIn) {
            // Chưa check-in
            if (now.isBefore(shiftStartTime)) {
                shiftSchedule.setStatus(""); // chưa đến giờ
                shiftSchedule.setLateMinutes(null);
            } else if (now.isAfter(shiftStartTime.plusMinutes(15))) {
                shiftSchedule.setStatus("Absent"); // quá 15 phút
                shiftSchedule.setLateMinutes(null);
            } else {
                shiftSchedule.setStatus(""); // trong 15 phút đầu
                shiftSchedule.setLateMinutes(null);
            }
        } else {
            // Đã check-in → tính dựa trên thời gian check-in
            long minutesLate = ChronoUnit.MINUTES.between(shiftStartTime, checkInTime);

            if (minutesLate <= 0) {
                shiftSchedule.setStatus("Present");
                shiftSchedule.setLateMinutes(null);
            } else if (minutesLate <= 15) {
                shiftSchedule.setStatus("Late");
                shiftSchedule.setLateMinutes((int) minutesLate);
            } else {
                shiftSchedule.setStatus("Absent");
                shiftSchedule.setLateMinutes(null);
            }
        }
    }

    public void checkInShift(String shiftId) {
        Optional<ShiftSchedule> optional = shiftScheduleRepository.findById(shiftId);
        if(optional.isPresent()) {
            ShiftSchedule shift = optional.get();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime shiftStart = LocalDateTime.of(shift.getShiftDate(), shift.getShiftStart());

            // Local variable để xử lý check-in
            boolean isCheckIn = true;

            long minutesLate = ChronoUnit.MINUTES.between(shiftStart, now);

            if(minutesLate <= 0) {
                shift.setStatus("Present");
                shift.setLateMinutes(null);
            } else if(minutesLate <= 15) {
                shift.setStatus("Late");
                shift.setLateMinutes((int) minutesLate);
            } else {
                shift.setStatus("Absent");
                shift.setLateMinutes(null);
            }

            // Lưu lại thay đổi
            shiftScheduleRepository.save(shift);
        }
    }

    public List<ShiftSchedule> findByShiftDate(LocalDate date) {
        return shiftScheduleRepository.findByShiftDate(date);
    }







}
