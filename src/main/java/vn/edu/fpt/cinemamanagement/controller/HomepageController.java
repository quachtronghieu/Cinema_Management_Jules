package vn.edu.fpt.cinemamanagement.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.cinemamanagement.entities.Movie;
import vn.edu.fpt.cinemamanagement.entities.Showtime;
import vn.edu.fpt.cinemamanagement.entities.Voucher;
import vn.edu.fpt.cinemamanagement.services.MovieService;
import vn.edu.fpt.cinemamanagement.services.ShowtimeService;
import vn.edu.fpt.cinemamanagement.services.VoucherService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import java.security.Principal;
import java.time.LocalDate;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/homepage")
public class HomepageController {
    @Autowired
    private MovieService movieService;
    @Autowired
    private VoucherService voucherService;
    @Autowired
    private ShowtimeService showtimeService;

    //Huynh Anh add
    @GetMapping({"", "/"})
    public String homepage(Model model, Principal principal, @RequestParam(name = "page", defaultValue = "1", required = false) int page) {
        int size = 10;
        Pageable pageable = PageRequest.of(page - 1, size);
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("username", username);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String role = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", "));
            model.addAttribute("role", role);
        }
        Page<Movie> movie = movieService.getAllMovies(pageable);
        model.addAttribute("movies", movie);
        return "homepage/homepage";
    }

    @GetMapping("/vouchers")
    public String vouchers(Model model, @RequestParam(name = "page", defaultValue = "1", required = false) int page) {
        int size = 9;

        // Spring Data bắt đầu từ 0
        int pageIndex = page - 1;
        Pageable pageable = PageRequest.of(pageIndex, size);
        Page<Voucher> voucherPage = voucherService.findAllVoucher(pageable);

        model.addAttribute("vouchersList", voucherPage.getContent());

        int totalPages = voucherPage.getTotalPages();
        int currentPage = page; // vẫn giữ 1-based cho Thymeleaf

        int visiblePages = 5;
        int startPage, endPage;

        if (totalPages <= visiblePages) {
            startPage = 1; // 1-based
            endPage = totalPages;
        } else {
            startPage = ((currentPage - 1) / visiblePages) * visiblePages + 1;
            endPage = Math.min(startPage + visiblePages - 1, totalPages);
        }

        model.addAttribute("voucherPage", voucherPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);
        return "vouchers/voucher_list_customer";
    }

    @GetMapping("/vouchers/detail/{id}")
    public String voucherDetails(@PathVariable("id") String id, Model model) {
        model.addAttribute("voucher", voucherService.findVoucherById(id));
        return "vouchers/voucher_detail_customer";
    }

    // 1. Add thêm movies now showing - Huynh Anh
    @GetMapping("/movies_nowshowing")
    public String moviesNowShowing(
            @CookieValue(value = "user_name", required = false) String username,
            @CookieValue(value = "user_role", required = false) String role,
            Model model) {
        // Lấy danh sách phim đang chiếu (có logic ẩn phim > 30 ngày )
        List<Movie> nowShowing = movieService.getNowShowingMovies();

        // Đưa danh sách và thông tin người dùng ra view
        model.addAttribute("nowShowing", nowShowing);
        model.addAttribute("username", username);
        model.addAttribute("role", role);

        // Render sang view mới
        return "movies/movies_nowshowing"; // trỏ tới file templates/movies/movies_nowshowing.html
    }

    @GetMapping("/coming-soon")
    public String comingSoon(Model model,
                             @RequestParam(name = "page", defaultValue = "1", required = false) int page) {
        int size = 8;

        int pageIndex = page - 1; // vì Spring Data bắt đầu từ 0
        Pageable pageable = PageRequest.of(pageIndex, size);
        Page<Movie> comingSoonPage = movieService.findComingSoonMovies(pageable);

        model.addAttribute("comingSoon", comingSoonPage.getContent());

        int totalPages = comingSoonPage.getTotalPages();
        int currentPage = page;

        int visiblePages = 5;
        int startPage, endPage;

        if (totalPages <= visiblePages) {
            startPage = 1;
            endPage = totalPages;
        } else {
            startPage = ((currentPage - 1) / visiblePages) * visiblePages + 1;
            endPage = Math.min(startPage + visiblePages - 1, totalPages);
        }

        model.addAttribute("comingSoonPage", comingSoonPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("totalPages", totalPages);

        return "movies/movie_coming_soon";
    }
    @RequestMapping(value = "/movie_detail/{id}")
    public String getMovieDetails(@PathVariable("id") String id, Model model) {
        Movie movie = movieService.findById(id);
        model.addAttribute("movie", movie);
        boolean isNowShowing = movieService.isMovieNowShowing(movie, movieService.getNowShowingMovies());
        if (isNowShowing) {
            model.addAttribute("isNowShowing", true);
        } else {
            model.addAttribute("isNowShowing", false);
        }
        if (!movieService.existsByMovieID(id)) {
            model.addAttribute("error", String.format("Movie with ID %s does not exist", id));
        }
        return "movies/movie_detail_guest";
    }

    @GetMapping("/showtimes")
    public String showtimeGuestPage(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        LocalDate selectedDate = (date != null) ? date : LocalDate.now();

        // 🔹 Tạo danh sách 7 ngày để người dùng chọn (TH2 -> CN)
        List<LocalDate> days = new ArrayList<>();
        for (int i = -3; i <= 3; i++) {
            days.add(selectedDate.plusDays(i));
        }

        // 🔹 Lấy danh sách showtime theo ngày
        List<Showtime> showtimes = showtimeService.getShowtimesByDate(selectedDate);

        // 🔹 Gom các suất chiếu theo phim
        Map<String, List<Showtime>> movieMap = showtimes.stream()
                .collect(Collectors.groupingBy(st -> st.getMovie().getMovieID()));

        // 🔹 Danh sách phim
        List<Movie> movieList = movieMap.keySet().stream()
                .map(id -> showtimes.stream()
                        .filter(st -> st.getMovie().getMovieID().equals(id))
                        .findFirst()
                        .map(Showtime::getMovie)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 🔹 Gom giờ chiếu theo phim & phòng
        Map<String, List<Map<String, Object>>> scheduleGroups = new HashMap<>();

        for (var entry : movieMap.entrySet()) {
            String movieId = entry.getKey();
            List<Showtime> list = entry.getValue();

            // nhóm theo room
            Map<String, List<Showtime>> byRoom = list.stream()
                    .collect(Collectors.groupingBy(st -> {
                        if (st.getRoom() != null && st.getRoom().getTemplate() != null) {
                            return st.getRoom().getTemplate().getName(); //  hiển thị template
                        } else if (st.getRoom() != null) {
                            return st.getRoom().getId(); // fallback nếu template null
                        } else {
                            return "Unknown Room";
                        }
                    }));

            List<Map<String, Object>> roomGroups = new ArrayList<>();

            for (var roomEntry : byRoom.entrySet()) {
                Map<String, Object> roomData = new HashMap<>();
                roomData.put("roomName", roomEntry.getKey());

                // danh sách khung giờ của room này
                List<Map<String, LocalTime>> slots = roomEntry.getValue().stream()
                        .sorted(Comparator.comparing(Showtime::getStartTime))
                        .map(st -> {
                            Map<String, LocalTime> timeSlot = new HashMap<>();
                            timeSlot.put("startTime", st.getStartTime());
                            timeSlot.put("endTime", st.getEndTime());
                            return timeSlot;
                        })
                        .collect(Collectors.toList());

                roomData.put("slots", slots);
                roomGroups.add(roomData);
            }
            scheduleGroups.put(movieId, roomGroups);
        }

        model.addAttribute("days", days);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("prevDate", selectedDate.minusDays(7));
        model.addAttribute("nextDate", selectedDate.plusDays(7));
        model.addAttribute("movieList", movieList);
        model.addAttribute("scheduleGroups", scheduleGroups);

        return "showtime/showtime_for_guest";
    }
}
