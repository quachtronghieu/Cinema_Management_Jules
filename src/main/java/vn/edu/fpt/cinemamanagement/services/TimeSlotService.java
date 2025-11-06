package vn.edu.fpt.cinemamanagement.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.cinemamanagement.entities.Movie;
import vn.edu.fpt.cinemamanagement.entities.Showtime;
import vn.edu.fpt.cinemamanagement.repositories.ShowtimeRepository;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TimeSlotService {

    private final ShowtimeRepository repo;

    public TimeSlotService(ShowtimeRepository repo) {
        this.repo = repo;
    }

    /** Tạo danh sách giờ chiếu cố định (09:00 → 23:45, cách nhau 15 phút) */
    public List<LocalTime> generateFixedSlots() {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime t = LocalTime.of(9, 0);
        while (!t.isAfter(LocalTime.of(23, 45))) {
            slots.add(t);
            t = t.plusMinutes(15);
        }
        return slots;
    }

    /** Làm tròn giờ về bội của 15 phút (00, 15, 30, 45) */
    private LocalTime roundToQuarter(LocalTime time) {
        int minute = time.getMinute();
        int rounded = (int) (Math.round(minute / 15.0) * 15);
        if (rounded == 60) {
            return time.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        }
        return time.withMinute(rounded).withSecond(0).withNano(0);
    }

    /** Tạo danh sách các mốc giờ (theo stepHours) từ [start, end), có làm tròn */
    public List<LocalTime> buildWindow(LocalTime start, LocalTime end, int stepHours) {
        List<LocalTime> out = new ArrayList<>();
        if (start == null) start = LocalTime.of(9, 0);
        if (end == null) end = LocalTime.of(23, 50);
        if (stepHours <= 0) stepHours = 3;

        LocalTime t = roundToQuarter(start);
        while (!t.isAfter(end)) {
            out.add(t);
            t = roundToQuarter(t.plusHours(stepHours));
        }
        return out;
    }

    /**
     * Trả về danh sách giờ bắt đầu còn trống (roomId + date),
     * tránh trùng với showtime đã có trong DB.
     */
    public List<LocalTime> availableStarts(String roomId, LocalDate date,
                                           LocalTime winStart, LocalTime winEnd,
                                           int stepHours, Duration assumedDuration) {

        // đảm bảo duration hợp lệ
        final Duration dur = (assumedDuration == null || assumedDuration.isZero() || assumedDuration.isNegative())
                ? Duration.ofHours(3)
                : assumedDuration;

        // lấy danh sách suất chiếu đã có trong ngày
        List<Showtime> dayList = repo.findByRoom_IdAndShowDate(roomId, date);

        // tạo danh sách mốc giờ tròn
        return buildWindow(winStart, winEnd, stepHours).stream()
                .filter(start -> {
                    LocalTime aStart = start;
                    LocalTime aEnd = aStart.plus(dur);
                    if (aEnd.isAfter(LocalTime.of(23, 50))) return false;

                    // loại bỏ slot trùng
                    return dayList.stream().noneMatch(st ->
                            overlaps(aStart, aEnd, st.getStartTime(), st.getEndTime()));
                })
                .collect(Collectors.toList());
    }

    /** Kiểm tra trùng giờ giữa 2 khoảng [aStart, aEnd) và [bStart, bEnd) */
    public boolean overlaps(LocalTime aStart, LocalTime aEnd,
                            LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }
    public List<Showtime> generateShowtimes(Movie movie, LocalDate date) {
        List<Showtime> slots = new ArrayList<>();
        if (movie == null) return slots;

        int duration = (movie.getDuration() > 0) ? movie.getDuration() : 120; // phút
        LocalTime start = LocalTime.of(9, 0);
        LocalTime endOfDay = LocalTime.of(23, 0);

        while (true) {
            LocalTime end = start.plusMinutes(duration);
            if (end.isAfter(endOfDay)) break;

            Showtime st = new Showtime();
            st.setMovie(movie);
            st.setShowDate(date);
            st.setStartTime(start);
            st.setEndTime(end);
            slots.add(st);

            // nghỉ 15 phút giữa các suất
            start = end.plusMinutes(15);
        }
        return slots;
    }

    public List<LocalDate> getWeekDates(LocalDate baseDate) {
        if (baseDate == null) baseDate = LocalDate.now();
        LocalDate startOfWeek = baseDate.minusDays(baseDate.getDayOfWeek().getValue() - 1); // Monday
        List<LocalDate> days = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            days.add(startOfWeek.plusDays(i));
        }
        return days;
    }

    /** Gom nhóm suất chiếu theo phim → phòng → các khung giờ */
    public Map<String, List<Map<String, Object>>> getGroupedShowtimes(LocalDate date) {
        List<Showtime> showtimes = repo.findByShowDate(date);
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();

        for (Showtime st : showtimes) {
            String movieId = st.getMovie().getMovieID();

            String roomName = st.getRoom().getTemplate().getName(); // hoặc getName(), getId() tùy entity thực tế

            grouped.putIfAbsent(movieId, new ArrayList<>());

            List<Map<String, Object>> roomList = grouped.get(movieId);
            Map<String, Object> roomGroup = roomList.stream()
                    .filter(r -> r.get("roomName").equals(roomName))
                    .findFirst()
                    .orElseGet(() -> {
                        Map<String, Object> newRoom = new HashMap<>();
                        newRoom.put("roomName", roomName);
                        newRoom.put("slots", new ArrayList<Map<String, Object>>());
                        roomList.add(newRoom);
                        return newRoom;
                    });

            List<Map<String, Object>> slots = (List<Map<String, Object>>) roomGroup.get("slots");
            Map<String, Object> slot = new HashMap<>();
            slot.put("startTime", st.getStartTime());
            slot.put("endTime", st.getEndTime());
            slots.add(slot);
        }
        return grouped;
    }
}
