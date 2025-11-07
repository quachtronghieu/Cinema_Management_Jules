package vn.edu.fpt.cinemamanagement.services;

import vn.edu.fpt.cinemamanagement.dto.RoomDetailDTO;
import vn.edu.fpt.cinemamanagement.entities.Room;
import vn.edu.fpt.cinemamanagement.entities.Template;
import vn.edu.fpt.cinemamanagement.repositories.RoomRepository;
import vn.edu.fpt.cinemamanagement.repositories.TemplateSeatRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private RoomRepository roomRepository;
    private TemplateSeatRepository templateSeatRepository;
    private TemplateSeatService templateSeatService;

    public RoomService(
            RoomRepository roomRepository,
            TemplateSeatRepository templateSeatRepository,
            TemplateSeatService templateSeatService) {
        this.roomRepository = roomRepository;
        this.templateSeatRepository = templateSeatRepository;
        this.templateSeatService = templateSeatService;
    }

 public List<Room> getAllRooms(){
return roomRepository.findAll();
 }
    @Transactional
    public List<RoomDetailDTO> getRoomDetails() {
        return roomRepository.findAll().stream()
                .map(room -> {
                    String templateName = room.getTemplate().getName();
                    String templateId = room.getTemplate().getId();
                    Long totalSeats = (long) templateSeatService.countTotalSeatsByTemplateID(templateId);

                    // Tạo DTO
                    RoomDetailDTO dto = new RoomDetailDTO(
                            room.getId(),
                            templateName,
                            totalSeats,
                            room.getStatus()
                    );
                    dto.setBuildRoomName(buildRoomName(room.getId()));

                    return dto;
                })
                .collect(Collectors.toList());
    }
 @Transactional
public String getRoomId(String roomID){
        Room room = roomRepository.findById(roomID).orElseThrow(()->new IllegalArgumentException("Room Not Found"));
        return room.getId();
}
@Transactional
    public Room findById(String id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng: " + id));
    }
    @Transactional
    public String getRoomStatus(String roomID) {
        Room r = roomRepository.findById(roomID)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng: " + roomID));
        return r.getStatus(); // "active" / "unactive"
    }

    @Transactional
    public String buildRoomName(String id) {
        if (id == null || id.isEmpty()) return "";
        String upper = id.toUpperCase();
        try {
            if (upper.startsWith("R")) {
                return "Room " + Integer.parseInt(id.substring(1));
            }
        } catch (NumberFormatException ignored) {}
        return "Room " + id; // fallback
    }
    @Transactional
    public Room updateRoom(Room room) {
        Room existingRoom = roomRepository.findById(room.getId())
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại: " + room.getId()));

        if (room.getTemplate() != null) {
            existingRoom.setTemplate(room.getTemplate());
        }
        if (room.getStatus() != null) {
            existingRoom.setStatus(room.getStatus());
        }

        return roomRepository.save(existingRoom);
    }
    @Transactional
    public Room createRoom(Room room) {
        if (roomRepository.existsById(room.getId())) {
            throw new IllegalArgumentException("Phòng với ID " + room.getId() + " đã tồn tại");
        }
        if (room.getTemplate() == null) {
            throw new IllegalArgumentException("Template không được để trống");
        }
        return roomRepository.save(room);
    }
    @Transactional
    public void deleteRoom(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại: " + roomId));
        roomRepository.delete(room);
    }
    public RoomDetailDTO getRoomDetailById(String roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phòng có ID: " + roomId));

        Template template = room.getTemplate();


        RoomDetailDTO dto = new RoomDetailDTO();
        dto.setRoomId(room.getId());
        dto.setTemplateName(template.getName());
        dto.setStatus(room.getStatus());

        return dto;
    }


}
