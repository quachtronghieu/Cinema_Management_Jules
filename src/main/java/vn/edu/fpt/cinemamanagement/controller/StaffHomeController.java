package vn.edu.fpt.cinemamanagement.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.cinemamanagement.entities.Movie;
import vn.edu.fpt.cinemamanagement.services.ShowtimeService;
import vn.edu.fpt.cinemamanagement.services.TimeSlotService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
public class StaffHomeController {

    private final ShowtimeService showtimeService;
    private final TimeSlotService timeSlotService;


    public StaffHomeController(ShowtimeService showtimeService, TimeSlotService timeSlotService) {
        this.showtimeService = showtimeService;
        this.timeSlotService = timeSlotService;
    }

    @GetMapping("/staff_home")
    public String staffHome(HttpServletRequest request, Model model) {
        String role = "Staff";
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("user_role".equals(c.getName())) {
                    role = c.getValue().replace("%20", " ");
                }
            }
        }
        model.addAttribute("role", role);
        return "dashboard/staff_home";
    }

    @GetMapping("/cashier/showtimes")
    public String showShowtimesForCashier(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Model model) {

        if (date == null) date = LocalDate.now();

        List<LocalDate> days = timeSlotService.getWeekDates(date);
        Map<String, List<Map<String, Object>>> scheduleGroups = timeSlotService.getGroupedShowtimes(date);
        List<Movie> movieList = showtimeService.getMoviesByDate(date);

        model.addAttribute("days", days);
        model.addAttribute("selectedDate", date);
        model.addAttribute("movieList", movieList);
        model.addAttribute("scheduleGroups", scheduleGroups);
        model.addAttribute("prevDate", date.minusDays(1));
        model.addAttribute("nextDate", date.plusDays(1));

        return "dashboard/showtime_for_cashier";
    }
}
