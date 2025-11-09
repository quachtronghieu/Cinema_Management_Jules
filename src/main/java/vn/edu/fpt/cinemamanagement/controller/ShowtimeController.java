package vn.edu.fpt.cinemamanagement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.cinemamanagement.dto.ShowtimeForm;
import vn.edu.fpt.cinemamanagement.entities.Movie;
import vn.edu.fpt.cinemamanagement.entities.Room;
import vn.edu.fpt.cinemamanagement.entities.Showtime;
import vn.edu.fpt.cinemamanagement.repositories.ShowtimeRepository;
import vn.edu.fpt.cinemamanagement.services.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/showtime")
public class ShowtimeController {

    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private TimeSlotService timeSlotService;
    @Autowired private ShowtimeSeatService showtimeSeatService;

    private final MovieService movieService;
    private final RoomService roomService;
    private final ShowtimeService showtimeService;

    public ShowtimeController(MovieService movieService,
                              RoomService roomService,
                              ShowtimeService showtimeService,
                              TimeSlotService timeSlotService) {
        this.movieService = movieService;
        this.roomService = roomService;
        this.showtimeService = showtimeService;
        this.timeSlotService = timeSlotService;
    }

    /**  LIST tất cả showtime */
    @GetMapping
    public String list(Model model) {
        List<Showtime> list = showtimeRepository.findAll();
        for (Showtime s : list) {
            s.getMovie().getTitle(); // ép Hibernate load 1 lần
            s.getRoom().getId();
        }
        model.addAttribute("showtimes", list);
        return "showtime/showtime_list";
    }

    /**  FORM tạo mới */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("form", new ShowtimeForm());
        model.addAttribute("movies", movieService.getNowShowingMovies());
        model.addAttribute("roomList", showtimeService.getAllRoomsWithTemplate());
        return "showtime/showtime_create";
    }

    @GetMapping("/check-available")
    @ResponseBody
    public Map<String, Object> checkAvailability(
            @RequestParam String movieId,
            @RequestParam String roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam int duration
    ) {
        LocalTime endTime = startTime.plusMinutes(duration);

        // Sửa repo → showtimeRepository
        boolean overlap = showtimeRepository.hasOverlapInRoom(roomId, date, startTime, endTime);
        boolean sameMovieOtherRoom = showtimeRepository.hasSameMovieInOtherRoom(movieId, roomId, date, startTime, endTime);

        String message;
        boolean available;

        if (overlap) {
            message = "This time slot is already taken for this room.";
            available = false;
        } else if (sameMovieOtherRoom) {
            message = "This movie is already scheduled in another room at this time.";
            available = false;
        } else {
            message = "This slot is available.";
            available = true;
        }

        return Map.of("available", available, "message", message);
    }

    /**  API load slot khả dụng */
    @GetMapping("/available-slots")
    @ResponseBody
    public ResponseEntity<List<String>> availableSlots(
            @RequestParam String roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "09:00") String start // windowStart
    ) {
        LocalTime winStart = LocalTime.parse(start);
        LocalTime winEnd = LocalTime.of(23, 59);
        int stepHours = 3;

        Duration assumed = Duration.ofHours(3);
        var result = timeSlotService.availableStarts(roomId, date, winStart, winEnd, stepHours, assumed)
                .stream()
                .map(LocalTime::toString)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**  POST tạo mới */
    @PostMapping("/create")
    public String createSubmit(@ModelAttribute("form") ShowtimeForm form,
                               Model model,
                               RedirectAttributes ra) {

        boolean hasError = false;
        Map<String, String> fieldErrors = new HashMap<>();

        // Validate thủ công từng ô
        if (form.getMovieId() == null || form.getMovieId().isEmpty()) {
            fieldErrors.put("movieIdError", "Please select a movie.");
            hasError = true;
        }
        if (form.getRoomId() == null || form.getRoomId().isEmpty()) {
            fieldErrors.put("roomIdError", "Please select a room.");
            hasError = true;
        }
        if (form.getShowDate() == null) {
            fieldErrors.put("showDateError", "Please select a date.");
            hasError = true;
        } else {
            LocalDate now = LocalDate.now();
            if (form.getShowDate().isBefore(now.minusYears(1))) {
                fieldErrors.put("showDateError", "Date cannot be more than 1 year in the past.");
                hasError = true;
            } else if (form.getShowDate().isAfter(now.plusYears(1))) {
                fieldErrors.put("showDateError", "Date cannot be more than 1 year in the future.");
                hasError = true;
            }
        }
        if (form.getStartTime() == null) {
            fieldErrors.put("startTimeError", "Please select a start time.");
            hasError = true;
        }

        //  Nếu có lỗi thì quay lại form
        if (hasError) {
            model.addAllAttributes(fieldErrors);
            model.addAttribute("movies", movieService.getNowShowingMovies());
            model.addAttribute("roomList", showtimeService.getAllRoomsWithTemplate());
            model.addAttribute("form", form);
            return "showtime/showtime_create"; //  render lại form
        }

        //  Nếu hợp lệ thì gọi service
        try {
            Showtime show = showtimeService.createShowtime(
                    form.getMovieId(),
                    form.getRoomId(),
                    form.getShowDate(),
                    form.getStartTime()
            );
            if (show != null) {
                showtimeSeatService.createShowtimeSeats(show.getShowtimeId(), show.getRoom().getTemplate().getId());
            }
            ra.addFlashAttribute("msg", "Showtime created successfully!");
            return "redirect:/showtime";
        } catch (Exception ex) {
            ra.addFlashAttribute("msg", "Create failed: " + ex.getMessage());
            return "redirect:/showtime/create";
        }
    }



    // ✏ EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable String id, Model model) {
        Showtime st = showtimeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found"));

        ShowtimeForm form = new ShowtimeForm();
        form.setShowtimeId(st.getShowtimeId());
        form.setMovieId(st.getMovie().getMovieID());
        form.setRoomId(st.getRoom().getId());
        form.setShowDate(st.getShowDate());
        form.setStartTime(st.getStartTime());

        model.addAttribute("form", form);
        model.addAttribute("movies", movieService.getNowShowingMovies());
        model.addAttribute("rooms", showtimeService.getAllRoomsWithTemplate());
        return "showtime/showtime_update";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable String id,
                         @ModelAttribute("form") ShowtimeForm form,
                         RedirectAttributes ra) {
        try {
            showtimeService.updateShowtime(
                    id,
                    form.getMovieId(),
                    form.getRoomId(),
                    form.getShowDate(),
                    form.getStartTime()
            );
            ra.addFlashAttribute("msg", "Showtime updated successfully!");
            return "redirect:/showtime";
        } catch (Exception e) {
            ra.addFlashAttribute("msg", "Update failed: " + e.getMessage());
            return "redirect:/showtime/edit/" + id;
        }
    }

    // 🗑 DELETE
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable String id, RedirectAttributes ra) {
        try {
            showtimeRepository.deleteById(id);
            ra.addFlashAttribute("msg", "Showtime deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("msg", "Delete failed: " + e.getMessage());
        }
        return "redirect:/showtime";
    }
    @GetMapping("/detail/{id}")
    public  String detailForm(@PathVariable String id, Model model) {
        Showtime showtime = showtimeService.showtimeByID(id);
        model.addAttribute("showtime", showtime);
        return "showtime/showtime_detail";
    }
}
