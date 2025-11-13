package vn.edu.fpt.cinemamanagement.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.entities.*;
import vn.edu.fpt.cinemamanagement.services.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping(value = "staffs")
public class StaffHomeController {
    @Autowired
    private final ShowtimeService showtimeService;
    private final TimeSlotService timeSlotService;
    private final TemplateSeatService templateSeatService;
    private CashierShowTimeSeatService cashierShowTimeSeatService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private TicketService ticketService;
    @Autowired
    private StaffService staffService;


    public StaffHomeController(ShowtimeService showtimeService, TimeSlotService timeSlotService, TemplateSeatService templateSeatService, CashierShowTimeSeatService cashierShowTimeSeatService) {
        this.showtimeService = showtimeService;
        this.timeSlotService = timeSlotService;
        this.templateSeatService = templateSeatService;
        this.cashierShowTimeSeatService = cashierShowTimeSeatService;
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

        return "cashier/showtime_for_cashier";
    }

    @GetMapping("/cashier/booking/{movieId}")
    public String showSeatMap(
            @PathVariable String movieId,
            @RequestParam String time,
            @RequestParam String date,
            Model model) {

        // Lấy suất chiếu cụ thể dựa trên movieId, date, time
        Showtime showtime = showtimeService.findByMovie(movieId, time, date);
        if (showtime == null) {
            return "redirect:/staffs/cashier/showtimes"; // fallback nếu không tìm thấy
        }

        // Lấy danh sách ghế (ShowtimeSeat) tương ứng
        String showtimeId = showtime.getShowtimeId();
        List<ShowtimeSeat> showtimeSeats = cashierShowTimeSeatService.createShowtimeSeats(showtimeId);

        // Tạo map trạng thái ghế: TemplateSeatID → status
        Map<String, String> seatStatusMap = showtimeSeats.stream()
                .collect(Collectors.toMap(
                        s -> s.getTemplateSeat().getId(),
                        ShowtimeSeat::getStatus
                ));

        // Gom nhóm theo hàng (A, B, C...) để hiển thị
        Map<String, List<ShowtimeSeat>> groupedSeats = showtimeSeats.stream()
                .sorted(Comparator.comparing(s -> s.getTemplateSeat().getSeatNumber()))
                .collect(Collectors.groupingBy(
                        s -> s.getTemplateSeat().getRowLabel(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        //  Truyền dữ liệu sang view
        model.addAttribute("groupSeat", groupedSeats);
        model.addAttribute("template", showtime.getRoom().getTemplate().getId());
        model.addAttribute("showtime", showtime);
        model.addAttribute("seatStatusMap", seatStatusMap); // thêm dòng này

        return "cashier/cashier_seatMap";
    }

    @GetMapping("/cashier/concessions")
    public String showConcessions(Model model) {
        model.addAttribute("concessions", cashierShowTimeSeatService.findAll());
        return "concession/concession_list_forCashier";
    }

    @PostMapping("/cashier/order")
    @ResponseBody
    public ResponseEntity<String> confirmOrder(@RequestParam MultiValueMap<String, String> formData) {
        try {
            String showtimeId = formData.getFirst("showtimeId");
            List<String> seatIds = formData.get("seatIds");
            List<String> concessionIds = formData.get("concessionIds");
            List<String> qtyList = formData.get("qty");

            bookingService.createBooking(showtimeId, seatIds, concessionIds, qtyList, "KH000000");

            return ResponseEntity.ok("Order confirmed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/checkIn_Ticket")
    public String checkInTicket(Model model) {
        model.addAttribute("tickets", ticketService.displayData());
        return "ticket/checkIn_Ticket";
    }

    @GetMapping("/checkIn_Ticket/{ticketId}")
    public String checkInTicket(Model model, @PathVariable String ticketId) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Staff staff = staffService.findByAccountUsername(user.getUsername());
        String error = ticketService.Checkin_Ticket(ticketId, staff);
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "redirect:/staffs/checkIn_Ticket";
    }

}
