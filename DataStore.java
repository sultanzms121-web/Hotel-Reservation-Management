import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStore implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Room> rooms = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();

    // New lists for pending and confirmed reservations
    private List<Reservation> pendingReservations = new ArrayList<>();
    private List<Reservation> confirmedReservations = new ArrayList<>();

    // --- GETTERS ---
    public List<Room> getRooms() {
        return rooms;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public List<Reservation> getPendingReservations() {
        return pendingReservations;
    }

    public List<Reservation> getConfirmedReservations() {
        return confirmedReservations;
    }

    // --- ADD METHODS ---
    public void addRoom(Room r) {
        rooms.add(r);
    }

    public void addCustomer(Customer c) {
        customers.add(c);
    }

    public void addReservation(Reservation r) {
        reservations.add(r);
    }

    public void addPendingReservation(Reservation r) {
        pendingReservations.add(r);
    }

    public void addConfirmedReservation(Reservation r) {
        confirmedReservations.add(r);
    }

    // Helper to get all reservations for validation
    public List<Reservation> getAllReservations() {
        List<Reservation> all = new ArrayList<>();
        all.addAll(reservations);
        all.addAll(pendingReservations);
        all.addAll(confirmedReservations);
        return all;
    }

    // --- THREE-FILE SEQUENTIAL ACCESS LOGIC ---
    public void saveAll(String custFile, String roomFile, String resFile) throws IOException {
        saveFile(customers, custFile);
        saveFile(rooms, roomFile);
        saveFile(reservations, resFile);
        saveFile(pendingReservations, "pending_" + resFile);
        saveFile(confirmedReservations, "confirmed_" + resFile);
    }

    public void loadAll(String custFile, String roomFile, String resFile) throws IOException, ClassNotFoundException {
        this.customers = loadFile(custFile);
        this.rooms = loadFile(roomFile);
        this.reservations = loadFile(resFile);
        this.pendingReservations = loadFile("pending_" + resFile);
        this.confirmedReservations = loadFile("confirmed_" + resFile);
    }

    private <T> void saveFile(List<T> list, String fileName) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(list);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> loadFile(String fileName) throws IOException, ClassNotFoundException {
        File file = new File(fileName);
        if (!file.exists()) return new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (List<T>) ois.readObject();
        }
    }
}
