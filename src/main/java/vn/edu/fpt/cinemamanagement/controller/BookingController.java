package vn.edu.fpt.cinemamanagement.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    @Autowired
    private BookingService bookingService;
    @Autowired
    private CustomerService customerService;

    @GetMapping("/{movieId}")
    public String viewShowtimeByMovie(
            @PathVariable("movieId") String movieId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        LocalDate selectedDate = (date != null) ? date : LocalDate.now();

        Movie movie = movieService.findById(movieId);
        if (movie == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Movie not found");
        }

        List<Showtime> showtimes = showtimeService.getShowtimesByMovieAndDate(movieId, selectedDate);

        // Gom giờ chiếu theo phòng
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
                    .toList();

            roomGroups.put(roomName, slots);
        });

        // Tạo danh sách ngày
        List<LocalDate> days = IntStream.rangeClosed(-3, 3)
                .mapToObj(i -> selectedDate.plusDays(i))
                .collect(Collectors.toList());

        // Thêm attribute cho view
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

        // Lấy danh sách ghế chiếu phim hiện tại (để check status)
        List<ShowtimeSeat> showtimeSeats = showtimeSeatService.getAllByShowtimeId(showtimeId);

        // Tạo map: TemplateSeatID → status (để dễ lookup)
        Map<String, String> seatStatusMap = showtimeSeats.stream()
                .collect(Collectors.toMap(s -> s.getTemplateSeat().getId(), ShowtimeSeat::getStatus));

        // Sắp xếp theo hàng, số ghế
        seats.sort(Comparator.comparing(TemplateSeat::getRowLabel)
                .thenComparing(TemplateSeat::getSeatNumber));

        Map<String, List<TemplateSeat>> groupedSeats = seats.stream()
                .collect(Collectors.groupingBy(TemplateSeat::getRowLabel,
                        LinkedHashMap::new, Collectors.toList()));

        // Truyền thêm map trạng thái xuống view
        model.addAttribute("showtime", showtimeService.showtimeByID(showtimeId));
        model.addAttribute("template", template.getId());
        model.addAttribute("groupSeat", groupedSeats);
        model.addAttribute("seatStatusMap", seatStatusMap);

        return "booking/seat_map";
    }


    @PostMapping("/concessions")
    public String concessionsPage(@RequestParam Map<String, String> params, Model model){
        model.addAttribute("concessions" , concessionService.findAll());

        Showtime showtime = showtimeService.showtimeByID(params.get("showtimeId"));

        model.addAttribute("selectedSeats", params.get("selectedSeats"));
        model.addAttribute("totalPrice", params.get("totalPrice"));
        model.addAttribute("showtime", showtime);
        model.addAttribute("endtime", params.get("endtime"));
        return "concession/concession_list_forCus";
    }

    @PostMapping("/book")
    public String bookingPage(@RequestParam Map<String, String> params, Model model) {

        // Lấy showtime từ service
        Showtime showtime = showtimeService.showtimeByID(params.get("showtimeId"));

        // Parse JSON concessionIds thành 2 list riêng
        List<String> concessionIds = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        String concessionJson = params.get("selectedConcessionIds");
        if (concessionJson != null && !concessionJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Map<String, Object>> concessions = mapper.readValue(
                        concessionJson,
                        new TypeReference<List<Map<String, Object>>>() {}
                );

                for (Map<String, Object> item : concessions) {
                    concessionIds.add((String) item.get("id"));
                    // Dữ liệu qty có thể là Integer hoặc Double tùy trình duyệt
                    Object qtyObj = item.get("qty");
                    int qty = (qtyObj instanceof Integer) ? (Integer) qtyObj : ((Number) qtyObj).intValue();
                    quantities.add(qty);
                }
            } catch (Exception e) {
                System.err.println("Error parsing selectedConcessionIds JSON: " + e.getMessage());
                e.printStackTrace();
            }
        }

        List<Concession> concessions = new ArrayList<>();
        if (!concessionIds.isEmpty()) {
            for (String concessionId : concessionIds) {
                 concessions.add(concessionService.findById(concessionId));
            }
        }


        // Truyền dữ liệu sang view
        model.addAttribute("selectedSeats", params.get("selectedSeats"));
        model.addAttribute("totalPrice", params.get("totalPrice"));
        model.addAttribute("showtime", showtime);
        model.addAttribute("endtime", params.get("endtime"));
        model.addAttribute("selectedConcessionIds", params.get("selectedConcessionIds"));
        model.addAttribute("concessionIds", concessionIds);
        model.addAttribute("concessions", concessions);
        model.addAttribute("quantities", quantities);

        return "booking/book";
    }

    @PostMapping("/payment")
    public String paymentPage(@RequestParam Map<String, String> params, Model model) {
        try {
            String showtimeId = params.get("showtimeId");

            List<String> seatIds = Arrays.stream(params.get("selectedSeats").split(","))
                    .map(String::trim).toList();

            // Làm sạch chuỗi JSON-like: [1, 2] -> 1, 2
            String rawConIds = params.get("concessionIds");
            String rawQtys = params.get("quantities");

            List<String> concessionIds = (rawConIds == null || rawConIds.isBlank())
                    ? List.of()
                    : Arrays.stream(rawConIds.replaceAll("[\\[\\]\\s]", "").split(","))
                    .filter(s -> !s.isEmpty())
                    .toList();

            List<String> qtyList = (rawQtys == null || rawQtys.isBlank())
                    ? List.of()
                    : Arrays.stream(rawQtys.replaceAll("[\\[\\]\\s]", "").split(","))
                    .filter(s -> !s.isEmpty())
                    .toList();

            // Xác thực
            System.out.println("Cleaned Concession IDs: " + concessionIds);
            System.out.println("Cleaned Quantities: " + qtyList);

            UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String userId = customerService.getCustomerByUsername(user.getUsername()).getUser_id();

            Booking booking = bookingService.createBooking(showtimeId, seatIds, concessionIds, qtyList, userId);
            model.addAttribute("booking", booking);

            return "booking/payment";
        } catch (Exception e) {
            System.err.println("Error parsing selectedConcessionIds JSON: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/booking/" + params.get("showtimeId");
        }
    }

}
