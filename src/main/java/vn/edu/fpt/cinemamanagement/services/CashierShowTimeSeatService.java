package vn.edu.fpt.cinemamanagement.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.cinemamanagement.entities.Concession;
import vn.edu.fpt.cinemamanagement.entities.Showtime;
import vn.edu.fpt.cinemamanagement.entities.ShowtimeSeat;
import vn.edu.fpt.cinemamanagement.repositories.ConcessionRepository;
import vn.edu.fpt.cinemamanagement.repositories.ShowtimeSeatRepository;

import java.util.List;

@Service
public class CashierShowTimeSeatService {
    @Autowired
    private ShowtimeSeatRepository showtimeSeatRepository;
    @Autowired
    private ConcessionRepository concessionRepository;
    @Autowired
    private ShowtimeSeatService showtimeSeatService;
    @Autowired
    private ShowtimeService showtimeService;

    @Transactional
    public List<ShowtimeSeat> createShowtimeSeats(String showtimeId) {
        Showtime showtime = showtimeService.showtimeByID(showtimeId);
        String templateId = showtime.getRoom().getTemplate().getId();

        // Xóa ghế cũ nếu không cùng template (phòng khác layout)
        List<ShowtimeSeat> seats = showtimeSeatRepository.getAllByShowtime_ShowtimeId(showtimeId);
        if (!seats.isEmpty()) {
            String oldTemplateId = seats.get(0).getTemplateSeat().getTemplate().getId();
            if (!oldTemplateId.equals(templateId)) {
                showtimeSeatRepository.deleteAll(seats);
                seats.clear();
            }
        }

        // Nếu chưa có ghế (hoặc vừa xóa vì khác template) thì tạo mới theo template của room hiện tại
        if (seats.isEmpty()) {
            showtimeSeatService.createShowtimeSeats(showtimeId, templateId);
            seats = showtimeSeatRepository.getAllByShowtime_ShowtimeId(showtimeId);
        }

        return seats;
    }

    /**
     * Khi nhấn Payment → đổi các ghế được chọn thành PENDING
     */
    @Transactional
    public void changeSeatStatus(String showtimeId, List<String> selectedSeatCodes) {
        List<ShowtimeSeat> seats = showtimeSeatRepository.getAllByShowtime_ShowtimeId(showtimeId);

        for (ShowtimeSeat seat : seats) {
            String code = seat.getTemplateSeat().getRowLabel() + seat.getTemplateSeat().getSeatNumber();
            if (selectedSeatCodes.contains(code) && "available".equals(seat.getStatus())) {
                seat.setStatus("pending");
            }
        }

        showtimeSeatRepository.saveAll(seats);
    }

    /**
     * Khi thanh toán thành công → đổi PENDING → UNAVAILABLE
     */
    @Transactional
    public void confirmPayment(String showtimeId, List<String> selectedSeatCodes) {
        List<ShowtimeSeat> seats = showtimeSeatRepository.getAllByShowtime_ShowtimeId(showtimeId);

        for (ShowtimeSeat seat : seats) {
            String code = seat.getTemplateSeat().getRowLabel() + seat.getTemplateSeat().getSeatNumber();
            if (selectedSeatCodes.contains(code) && "PENDING".equals(seat.getStatus())) {
                seat.setStatus("UNAVAILABLE");
            }
        }

        showtimeSeatRepository.saveAll(seats);
    }

    /**
     * Khi countdown hết mà chưa thanh toán → trả lại AVAILABLE
     */
    @Transactional
    public void releaseStatusSeats(String showtimeId) {
        List<ShowtimeSeat> seats = showtimeSeatRepository.getAllByShowtime_ShowtimeId(showtimeId);
        for (ShowtimeSeat seat : seats) {
            if ("PENDING".equals(seat.getStatus())) {
                seat.setStatus("AVAILABLE");
            }
        }
        showtimeSeatRepository.saveAll(seats);
    }
    @Transactional
    public List<Concession> findAll()
    {
        return concessionRepository.findAll();
    }


}

