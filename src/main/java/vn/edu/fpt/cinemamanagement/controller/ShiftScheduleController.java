package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.entities.ShiftSchedule;
import vn.edu.fpt.cinemamanagement.services.ShiftScheduleService;
import vn.edu.fpt.cinemamanagement.services.StaffService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/shift-schedules")
public class ShiftScheduleController {

    @Autowired
    private ShiftScheduleService shiftScheduleService;
    @Autowired
    private StaffService staffService;

    // --- List all shift schedules ---
    @GetMapping
    public String getAllSchedules(
            Model model,
            @RequestParam(name = "date", required = false) LocalDate date) {

        // Nếu không chọn date → mặc định là hôm nay
        if (date == null) {
            date = LocalDate.now();
        }

        // Lấy danh sách ca của ngày đó (không cần pageable vì 1 ngày thường ít ca)
        List<ShiftSchedule> schedules = shiftScheduleService.findByShiftDate(date);
        model.addAttribute("shiftSchedules", schedules);
        model.addAttribute("currentDate", date);

        // Tính ngày trước / ngày sau
        model.addAttribute("prevDate", date.minusDays(1));
        model.addAttribute("nextDate", date.plusDays(1));

        return "schedules/shift_schedule_list";
    }


    @GetMapping("/edit/{id}")
    public String editShiftSchedule(@PathVariable("id") String id, Model model) {
        Optional<ShiftSchedule> optional = shiftScheduleService.getShiftScheduleById(id);
        if (optional.isPresent()) {
            model.addAttribute("shiftSchedule", optional.get()); // đưa dữ liệu vào form
            model.addAttribute("staffList", staffService.getAllStaff()); // dropdown nhân viên
            return "schedules/shift_schedule_update";
        }
        return "redirect:/shift-schedules";
    }

    // POST: save trực tiếp, không validate
    @PostMapping("/update")
    public String updateShift(@ModelAttribute("shiftSchedule") ShiftSchedule shiftSchedule,
                              @RequestParam("staffId") String staffId,
                              Model model) {
        try {
            shiftSchedule.setStaff(staffService.getStaffByID(staffId));
            shiftScheduleService.save(shiftSchedule); // validate ngày trong service
            return "redirect:/shift-schedules"; // thành công → quay về danh sách
        } catch (IllegalArgumentException e) {
            // lỗi validate → giữ form, hiển thị message
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("staffList", staffService.getAllStaff());
            model.addAttribute("shiftSchedule", shiftSchedule);
            return "schedules/shift_schedule_update";
        }
    }


    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("shiftSchedule", new ShiftSchedule());
        model.addAttribute("staffList", staffService.getAllStaff());

        LocalDate minDate = LocalDate.now().plusDays(1); // từ ngày mai
        LocalDate maxDate = LocalDate.now().plusDays(7); // tối đa 7 ngày sau

        model.addAttribute("minDate", minDate);
        model.addAttribute("maxDate", maxDate);

        return "schedules/shift_schedule_create";
    }


    @PostMapping("/create")
    public String createShift(@ModelAttribute("shiftSchedule") ShiftSchedule shiftSchedule,
                              @RequestParam("staffId") String staffId,
                              Model model) {
        try {
            shiftSchedule.setStaff(staffService.getStaffByID(staffId));
            shiftScheduleService.save(shiftSchedule); // trong save đã validate và update status
            return "redirect:/shift-schedules";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("staffList", staffService.getAllStaff());
            model.addAttribute("shiftSchedule", shiftSchedule);
            return "schedules/shift_schedule_create";
        }
    }

    @PostMapping("/checkin/{id}")
    @ResponseBody
    public String checkInShift(@PathVariable("id") String id) {
        shiftScheduleService.checkInShift(id);
        return "redirect:/shift-schedules";
    }




    @PostMapping("/delete")
    public String deleteShift(@RequestParam("shiftScheduleId") String id) {
        shiftScheduleService.deleteShiftSchedule(id);
        return "redirect:/shift-schedules";
    }





}
