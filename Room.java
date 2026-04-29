// Room.java
import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RoomStatus {
        AVAILABLE,
        BOOKED
    }

    private String roomId;
    private RoomType type;
    private double pricePerNight;
    private RoomStatus status;   // ✅ REQUIRED

    public Room(String roomId, RoomType type, double pricePerNight) {
        this.roomId = roomId;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.status = RoomStatus.AVAILABLE; // default
    }

    public String getRoomId() {
        return roomId;
    }

    public RoomType getType() {
        return type;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return roomId + " (" + type + ", " + pricePerNight + ", " + status + ")";
    }
}
