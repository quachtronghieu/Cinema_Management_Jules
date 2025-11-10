package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import vn.edu.fpt.cinemamanagement.entities.*;
import vn.edu.fpt.cinemamanagement.services.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private ShowtimeSeatService  showtimeSeatService;
    @Autowired
    private RoomService roomService;
    @Autowired
    private TemplateSeatService templateSeatService;
    @Autowired
    private MovieService movieService;
    @Autowired
    private ConcessionService concessionService;
    @Autowired
    private ShowtimeService showtimeService;

    @GetMapping("/{movieId}")
    public String viewShowtimeByMovie(
            @PathVariable("movieId") String movieId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        LocalDate selectedDate = (date != null) ? date : LocalDate.now();

        // ✅ Lấy phim theo ID
        Movie movie = movieService.findById(movieId);
        if (movie == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found");
        }

        // ✅ Lấy showtime theo phim và ngày
        List<Showtime> showtimes = showtimeService.getShowtimesByMovieAndDate(movieId, selectedDate);

        // ✅ Gom giờ chiếu theo phòng
        Map<String, List<Map<String, Object>>> roomGroups = new HashMap<>();
        Map<String, List<Showtime>> byRoom = showtimes.stream()
                .collect(Collectors.groupingBy(st -> st.getRoom().getTemplate().getName()));

        byRoom.forEach((roomName, list) -> {
            List<Map<String, Object>> slots = list.stream()
                    .sorted(Comparator.comparing(Showtime::getStartTime))
                    .map(st -> Map.<String, Object>of(
                            "showtimeId", st.getShowtimeId(),
                            "startTime", st.getStartTime(),
                            "endTime", st.getEndTime()
                    ))
                    .toList(); // ✅ Chuẩn JDK 16+, compile ngon trên IntelliJ

            roomGroups.put(roomName, slots);
        });

        // ✅ Tạo danh sách ngày
        List<LocalDate> days = IntStream.rangeClosed(-3, 3)
                .mapToObj(i -> selectedDate.plusDays(i))
                .collect(Collectors.toList());

        // ✅ Thêm attribute cho view
        model.addAttribute("movie", movie);
        model.addAttribute("scheduleGroups", roomGroups);
        model.addAttribute("days", days);
        model.addAttribute("prevDate", selectedDate.minusDays(7));
        model.addAttribute("nextDate", selectedDate.plusDays(7));
        model.addAttribute("selectedDate", selectedDate);

        return "booking/showtime";
    }



    @GetMapping("/seatMap/{showtimeId}")
    public String viewSeatMap(@PathVariable("showtimeId") String showtimeId, Model model) {
        Template template = showtimeService.showtimeByID(showtimeId).getRoom().getTemplate();
        List<TemplateSeat> seats = templateSeatService.findAllSeatsByTemplateID(template.getId());

        // ✅ Lấy danh sách ghế chiếu phim hiện tại (để check status)
        List<ShowtimeSeat> showtimeSeats = showtimeSeatService.getAllByShowtimeId(showtimeId);

        // ✅ Tạo map: TemplateSeatID → status (để dễ lookup)
        Map<String, String> seatStatusMap = showtimeSeats.stream()
                .collect(Collectors.toMap(s -> s.getTemplateSeat().getId(), ShowtimeSeat::getStatus));

        // ✅ Sắp xếp theo hàng, số ghế
        seats.sort(Comparator.comparing(TemplateSeat::getRowLabel)
                .thenComparing(TemplateSeat::getSeatNumber));

        Map<String, List<TemplateSeat>> groupedSeats = seats.stream()
                .collect(Collectors.groupingBy(TemplateSeat::getRowLabel,
                        LinkedHashMap::new, Collectors.toList()));

        // ✅ Truyền thêm map trạng thái xuống view
        model.addAttribute("template", template.getId());
        model.addAttribute("groupSeat", groupedSeats);
        model.addAttribute("seatStatusMap", seatStatusMap);

        return "booking/seat_map";
    }



    @GetMapping("/concessions")
    public String concessionsPage(Model model){
        model.addAttribute("concessions" , concessionService.findAll());
        return "concession/concession_list_forCus";
    }

}
