package vn.edu.fpt.cinemamanagement.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cinemamanagement.entities.Showtime;
import vn.edu.fpt.cinemamanagement.entities.ShowtimeSeat;
import vn.edu.fpt.cinemamanagement.entities.TemplateSeat;
import vn.edu.fpt.cinemamanagement.repositories.ShowtimeSeatRepository;
import vn.edu.fpt.cinemamanagement.repositories.TemplateSeatRepository;

import java.util.List;

@Service
public class ShowtimeSeatService {

    @Autowired
    private ShowtimeSeatRepository showtimeSeatRepository;
    @Autowired
    private TemplateSeatService templateSeatService;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private ShowtimeService showtimeService;

    @Transactional
    public List<ShowtimeSeat> getAllByShowtimeId(String showtimeId){
        return showtimeSeatRepository.getAllByShowtime_ShowtimeId(showtimeId);
    }

    private String generateId() {
        String prefix = "HS";
        String lastId = showtimeSeatRepository.findLastId();

        if (lastId == null) {
            return prefix + "000001";
        }

        // Lấy phần số ở cuối ID
        int number = Integer.parseInt(lastId.substring(prefix.length())) + 1;

        // Format lại đúng 6 chữ số sau prefix
        return String.format("%s%06d", prefix, number);
    }

    public void createShowtimeSeats(String showtimeId, String templateId) {
        List<TemplateSeat> templateSeats = templateSeatService.findAllSeatsByTemplateID(templateId);
        for (TemplateSeat templateSeat : templateSeats) {
            ShowtimeSeat seat = new ShowtimeSeat();
            seat.setShowtimeSeatID(generateId());
            seat.setShowtime(showtimeService.showtimeByID(showtimeId));
            seat.setTemplateSeat(templateSeat);
            seat.setStatus("available");
            showtimeSeatRepository.save(seat);
        }
    }
}
