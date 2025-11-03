package vn.edu.fpt.cinemamanagement.controller;

import vn.edu.fpt.cinemamanagement.dto.RoomDetailDTO;
import vn.edu.fpt.cinemamanagement.entities.Room;
import vn.edu.fpt.cinemamanagement.entities.Template;
import vn.edu.fpt.cinemamanagement.repositories.TemplateRepository;
import vn.edu.fpt.cinemamanagement.repositories.TemplateSeatRepository;
import vn.edu.fpt.cinemamanagement.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping(value = "rooms")
public class RoomController {
    @Autowired
    private RoomService roomService;
    @Autowired
    private TemplateRepository templateRepository;
    @Autowired
    private TemplateSeatRepository templateSeatRepository;

    public RoomController(
            RoomService roomService,
            TemplateRepository templateRepository,
            TemplateSeatRepository templateSeatRepository) {
        this.roomService = roomService;
        this.templateRepository = templateRepository;
        this.templateSeatRepository = templateSeatRepository;
    }

    @GetMapping
    public String showRoomPage(Model model) {
        List<RoomDetailDTO> rooms = roomService.getRoomDetails();
        model.addAttribute("rooms", rooms);
        System.out.println("Loaded rooms: " + rooms);
        return "rooms/room_list";
    }

    @GetMapping("/edit/{roomId}")
    public String showEditRoomPage(@PathVariable("roomId") String roomId, Model model) {
        try {
            // Lấy thông tin chi tiết phòng
            RoomDetailDTO room = roomService.getRoomDetailById(roomId);

            // Gửi dữ liệu sang view edit_room.html
            model.addAttribute("room", room);
            model.addAttribute("templates", templateRepository.findAll());

            return "rooms/edit_room"; // render file templates/edit_room.html
        } catch (Exception e) {
            // Nếu lỗi (phòng không tồn tại)
            model.addAttribute("error", "Không tìm thấy phòng có ID: " + roomId);
            return "redirect:/rooms";
        }
    }

    @PostMapping("/save")
    public String saveRoom(@ModelAttribute RoomDetailDTO dto) {
        try {
            Room room = roomService.findById(dto.getRoomId());

            // Lấy Template theo tên mà người dùng chọn (type)
            Template template = templateRepository.findByName(dto.getTemplateName());
            if (template == null) {
                throw new IllegalArgumentException("Không tìm thấy template: " + dto.getTemplateName());
            }

            // Cập nhật type (template) và status
            room.setTemplate(template);
            room.setStatus(dto.getStatus());

            //  Lưu lại
            roomService.updateRoom(room);

            System.out.println("Room " + dto.getRoomId()
                    + " updated → Type: " + dto.getTemplateName()
                    + ", Status: " + dto.getStatus());

        } catch (Exception e) {
            System.err.println("Error saving room: " + e.getMessage());
        }

        return "redirect:/rooms";
    }


    @GetMapping("/delete/{roomId}")
    public String deleteRoom(@PathVariable("roomId") String roomId,
                             RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteRoom(roomId);
            redirectAttributes.addFlashAttribute("message", "Xóa phòng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    @GetMapping("seat")
    public String showSeatPage(Model model) {
        return "seats/seat_template";
    }
}
