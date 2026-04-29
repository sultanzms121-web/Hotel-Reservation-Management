// Reservation.java
import java.io.Serializable;
import java.time.LocalDate;

public class Reservation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reservationId;
    private Customer customer;
    private Room room;
    private LocalDate checkIn;
    private LocalDate checkOut;

    public Reservation(String reservationId, Customer customer, Room room,
                       LocalDate checkIn, LocalDate checkOut) {
        this.reservationId = reservationId;
        this.customer = customer;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }
    public void setRoom(Room room) {
    this.room = room;
}

    public String getReservationId() {
        return reservationId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    @Override
    public String toString() {
        return reservationId + " : " + customer.getName()
                + " -> Room " + room.getRoomId()
                + " (" + checkIn + " to " + checkOut + ")";
    }
}
