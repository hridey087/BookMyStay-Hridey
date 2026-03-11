import java.util.*;

public class BookMyStay {

    // ---------------- Room Domain Model ----------------
    static abstract class Room {
        private String type;

        public Room(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

    static class SingleRoom extends Room {
        public SingleRoom() { super("Single"); }
    }

    static class DoubleRoom extends Room {
        public DoubleRoom() { super("Double"); }
    }

    static class SuiteRoom extends Room {
        public SuiteRoom() { super("Suite"); }
    }

    // ---------------- Booking Request ----------------
    static class Reservation {
        private String guestName;
        private String roomType;

        public Reservation(String guestName, String roomType) {
            this.guestName = guestName;
            this.roomType = roomType;
        }

        public String getGuestName() {
            return guestName;
        }

        public String getRoomType() {
            return roomType;
        }
    }

    // ---------------- Inventory Management ----------------
    static class RoomInventory {
        private HashMap<String, Integer> inventory;

        public RoomInventory() {
            inventory = new HashMap<>();
            inventory.put("Single", 5);
            inventory.put("Double", 3);
            inventory.put("Suite", 2);
        }

        public int getAvailability(String roomType) {
            return inventory.getOrDefault(roomType, 0);
        }

        public boolean allocateRoom(String roomType) {
            int available = inventory.getOrDefault(roomType, 0);
            if (available > 0) {
                inventory.put(roomType, available - 1);
                return true;
            }
            return false;
        }

        public void displayInventory() {
            System.out.println("===== Current Inventory =====");
            for (Map.Entry<String, Integer> entry : inventory.entrySet()) {
                System.out.println(entry.getKey() + " Rooms Available: " + entry.getValue());
            }
            System.out.println("-----------------------------------");
        }
    }

    // ---------------- Booking Queue ----------------
    static class BookingQueue {
        private Queue<Reservation> queue;

        public BookingQueue() {
            queue = new LinkedList<>();
        }

        public void addRequest(Reservation reservation) {
            queue.add(reservation);
            System.out.println("Booking request added for: " + reservation.getGuestName());
        }

        public Reservation pollNext() {
            return queue.poll();
        }

        public boolean isEmpty() {
            return queue.isEmpty();
        }
    }

    // ---------------- Booking Service ----------------
    static class BookingService {
        private RoomInventory inventory;
        private Map<String, Set<String>> allocatedRooms; // RoomType -> Set of room IDs
        private int roomIdCounter;

        public BookingService(RoomInventory inventory) {
            this.inventory = inventory;
            this.allocatedRooms = new HashMap<>();
            this.roomIdCounter = 1;
        }

        public void processReservation(Reservation res) {
            String type = res.getRoomType();
            if (inventory.getAvailability(type) <= 0) {
                System.out.println("No " + type + " rooms available for " + res.getGuestName());
                return;
            }

            // Generate unique room ID
            String roomId = type.substring(0,1).toUpperCase() + String.format("%03d", roomIdCounter++);
            allocatedRooms.putIfAbsent(type, new HashSet<>());
            Set<String> roomSet = allocatedRooms.get(type);

            // Ensure uniqueness (Set ensures this)
            if (!roomSet.contains(roomId)) {
                roomSet.add(roomId);
                inventory.allocateRoom(type);
                System.out.println("Reservation Confirmed: " + res.getGuestName() +
                        " -> " + type + " Room [" + roomId + "]");
            }
        }

        public void displayAllocatedRooms() {
            System.out.println("\n===== Allocated Rooms =====");
            for (Map.Entry<String, Set<String>> entry : allocatedRooms.entrySet()) {
                System.out.println(entry.getKey() + " Rooms Assigned: " + entry.getValue());
            }
            System.out.println("-----------------------------------");
        }
    }

    // ---------------- Application Entry ----------------
    public static void main(String[] args) {

        // Initialize inventory and booking queue
        RoomInventory inventory = new RoomInventory();
        BookingQueue bookingQueue = new BookingQueue();

        // Guests submit booking requests
        bookingQueue.addRequest(new Reservation("Alice", "Single"));
        bookingQueue.addRequest(new Reservation("Bob", "Double"));
        bookingQueue.addRequest(new Reservation("Charlie", "Suite"));
        bookingQueue.addRequest(new Reservation("Diana", "Single"));
        bookingQueue.addRequest(new Reservation("Eve", "Suite")); // This will fail if only 2 suites

        // Initialize Booking Service
        BookingService bookingService = new BookingService(inventory);

        // Process all queued requests (FIFO)
        System.out.println("\nProcessing Booking Requests:\n");
        while (!bookingQueue.isEmpty()) {
            Reservation res = bookingQueue.pollNext();
            bookingService.processReservation(res);
        }

        // Show final allocated rooms and inventory
        bookingService.displayAllocatedRooms();
        inventory.displayInventory();
    }
}