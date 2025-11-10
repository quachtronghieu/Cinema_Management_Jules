package vn.edu.fpt.cinemamanagement.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.cinemamanagement.entities.Movie;
import vn.edu.fpt.cinemamanagement.entities.Room;
import vn.edu.fpt.cinemamanagement.entities.Showtime;
import vn.edu.fpt.cinemamanagement.entities.Template;
import vn.edu.fpt.cinemamanagement.repositories.ShowtimeRepository;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShowtimeService {

    private final ShowtimeRepository repo;
    private final MovieService movieService;
    private final RoomService roomService;
    private final TimeSlotService timeSlotService;

    public ShowtimeService(ShowtimeRepository repo,
                           MovieService movieService,
                           RoomService roomService,
                           TimeSlotService timeSlotService) {
        this.repo = repo;
        this.movieService = movieService;
        this.roomService = roomService;
        this.timeSlotService = timeSlotService;
    }


    @Transactional
    public Showtime createShowtime(String movieId, String roomId, LocalDate showDate, LocalTime startTime) {
        Movie movie = movieService.findById(movieId);
        Room room = roomService.findById(roomId);
        if (movie == null || room == null)
            throw new IllegalArgumentException("Movie or Room not found.");

        LocalDate now = LocalDate.now();

        if (showDate.isBefore(now.minusMonths(1))) {
            throw new IllegalArgumentException("Showtime date cannot be more than 1 months in the past.");
        }
        if (showDate.isAfter(now.plusMonths(1))) {
            throw new IllegalArgumentException("Showtime date cannot be more than 1 months in the future.");
        }
        if (showDate.isBefore(now.minusYears(1))) {
            throw new IllegalArgumentException("Showtime date cannot be more than 1 year in the past.");
        }
        if (showDate.isAfter(now.plusYears(1))) {
            throw new IllegalArgumentException("Showtime date cannot be more than 1 year in the future.");
        }

        int duration = movie.getDuration();
        int roundedDuration = (int) (Math.ceil(duration / 5.0) * 5);
        LocalTime endTime = startTime.plusMinutes(roundedDuration);

        boolean overlap = repo.hasOverlapInRoom(roomId, showDate, startTime, endTime);
        if (overlap) {
            throw new IllegalArgumentException("This time slot is already taken for this room. Please select another start time.");
        }

        boolean sameMovieInOtherRoom = repo.hasSameMovieInOtherRoom(movieId, roomId, showDate, startTime, endTime);
        if (sameMovieInOtherRoom) {
            throw new IllegalArgumentException("This movie is already scheduled in another room at this time. Please choose a different time or movie.");
        }

        Showtime st = new Showtime();
        st.setShowtimeId(generateShowtimeId());
        st.setMovie(movie);
        st.setRoom(room);
        st.setShowDate(showDate);
        st.setStartTime(startTime);
        st.setEndTime(endTime);
        return repo.save(st);
    }

    private String generateShowtimeId() {
        String lastId = repo.findTopByOrderByShowtimeIdDesc()
                .map(Showtime::getShowtimeId)
                .orElse(null);

        int next = 1;
        if (lastId != null && lastId.startsWith("SW")) {
            try {
                next = Integer.parseInt(lastId.substring(2)) + 1;
            } catch (NumberFormatException ignored) {}
        }

        return String.format("SW%05d", next);
    }

    public List<Showtime> getAll() {
        return repo.findAll();
    }


    public List<Movie> getMoviesByDate(LocalDate date) {
        List<Showtime> shows = repo.findByShowDate(date);
        return shows.stream()
                .map(Showtime::getMovie)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<Showtime> getShowtimesByDate(LocalDate date) {
        List<Showtime> showtimes = repo.findByShowDate(date);

        showtimes.forEach(st -> {
            if (st.getRoom() != null && st.getRoom().getTemplate() != null) {
                st.getRoom().getTemplate().getName();
            }
        });

        return showtimes;
    }

    public List<Room> getAllRoomsWithTemplate() {
        List<Room> rooms = roomService.getAllRooms(); // hoặc roomService.findAll()
        for (Room r : rooms) {
            if (r.getTemplate() != null) {
                r.getTemplate().getName(); // ép Hibernate load template
            }
            System.out.println("ROOM DEBUG: " + r.getId() +
                    " → template = " + (r.getTemplate() != null ? r.getTemplate().getName() : "null"));
        }
        return rooms;
    }



    @Transactional
    public Showtime updateShowtime(String showtimeId, String movieId, String roomId,
                                   LocalDate showDate, LocalTime startTime) {

        Showtime existing = repo.findById(showtimeId)
                .orElseThrow(() -> new IllegalArgumentException("Showtime not found."));

        Movie movie = movieService.findById(movieId);
        Room room = roomService.findById(roomId);
        if (movie == null || room == null)
            throw new IllegalArgumentException("Movie or Room not found.");

        LocalDate now = LocalDate.now();
        if (showDate.isBefore(now.minusMonths(1)))
            throw new IllegalArgumentException("Showtime date cannot be more than 1 months in the past.");
        if (showDate.isAfter(now.plusMonths(1)))
            throw new IllegalArgumentException("Showtime date cannot be more than 1 months in the future.");
        if (showDate.isBefore(now.minusYears(1))) {
            throw new IllegalArgumentException("Showtime date cannot be more than 1 year in the past.");
        }
        if (showDate.isAfter(now.plusYears(1))) {
            throw new IllegalArgumentException("Showtime date cannot be more than 1 year in the future.");
        }

        int duration = movie.getDuration();
        int roundedDuration = (int) (Math.ceil(duration / 5.0) * 5);
        LocalTime endTime = startTime.plusMinutes(roundedDuration);

        boolean overlap = repo.hasOverlapInRoom(roomId, showDate, startTime, endTime);
        if (overlap && !existing.getShowtimeId().equals(showtimeId)) {
            throw new IllegalArgumentException("This time slot is already taken for this room.");
        }

        boolean sameMovieOtherRoom = repo.hasSameMovieInOtherRoom(movieId, roomId, showDate, startTime, endTime);
        if (sameMovieOtherRoom)
            throw new IllegalArgumentException("This movie is already scheduled in another room at this time.");

        existing.setMovie(movie);
        existing.setRoom(room);
        existing.setShowDate(showDate);
        existing.setStartTime(startTime);
        existing.setEndTime(endTime);

        return repo.save(existing);
    }

    /**  Gom nhóm suất chiếu theo phim → phòng → các khung giờ, hiển thị đúng template name */
    public Map<String, List<Map<String, Object>>> getGroupedShowtimes(LocalDate date) {
        List<Showtime> showtimes = repo.findByShowDate(date);
        Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();

        for (Showtime st : showtimes) {
            String movieId = st.getMovie().getMovieID();

            //  Lấy đúng tên template của phòng
            String tempRoomName = "Unknown Room";
            if (st.getRoom() != null && st.getRoom().getTemplate() != null) {
                tempRoomName = st.getRoom().getTemplate().getName();
            } else if (st.getRoom() != null) {
                tempRoomName = st.getRoom().getId(); // fallback nếu template null
            }

            //  Phải gán thành final để dùng trong lambda
            final String roomName = tempRoomName;

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

    public Showtime showtimeByID(String id){
        return repo.findByShowtimeId(id);
    }


    public List<Showtime> getShowtimesByMovieAndDate(String movieId, LocalDate selectedDate) {
        return repo.findAllByMovie_MovieIDAndShowDate(movieId, selectedDate);
    }
    public Showtime findByMovie(String movieId , String times, String  dates){
        LocalDate date= LocalDate.parse(dates);
        LocalTime time = LocalTime.parse(times, DateTimeFormatter.ofPattern("HH:mm"));
        return repo.findMovieAndDateTime(movieId, date, time);
    }
}
