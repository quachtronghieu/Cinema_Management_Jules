package vn.edu.fpt.cinemamanagement.dto;

public class RoomDetailDTO {
    private String roomId;
    private String templateName;
    private Long totalSeats;
    private String status;
    private String buildRoomName;

    public RoomDetailDTO(String roomId, String templateName, Long totalSeats, String status) {
        this.roomId = roomId;
        this.templateName = templateName;
        this.totalSeats = totalSeats;
        this.status = status;
    }

    public RoomDetailDTO() {
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getTemplateName() {
        return templateName;
    }
    

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Long getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Long totalSeats) {
        this.totalSeats = totalSeats;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public String getBuildRoomName() {
        return buildRoomName;
    }

    public void setBuildRoomName(String buildRoomName) {
        this.buildRoomName = buildRoomName;
    }
}
