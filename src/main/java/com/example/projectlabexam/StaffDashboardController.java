package com.example.projectlabexam;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import javafx.event.ActionEvent;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StaffDashboardController {

    // Room View Tab
    @FXML private TableView<Room> roomTable;
    @FXML private TableColumn<Room, String> roomNumberColumn;
    @FXML private TableColumn<Room, String> roomTypeColumn;
    @FXML private TableColumn<Room, String> statusColumn;
    @FXML private TableColumn<Room, String> lastCleanedColumn;
    @FXML private TableColumn<Room, Void> actionsColumn;

    // Booking View Tab
    @FXML private TableView<Booking> bookingTable;
    @FXML private TableColumn<Booking, String> bookingIdColumn;
    @FXML private TableColumn<Booking, String> customerNameColumn;
    @FXML private TableColumn<Booking, String> bookingRoomColumn;
    @FXML private TableColumn<Booking, String> checkInColumn;
    @FXML private TableColumn<Booking, String> checkOutColumn;
    @FXML private TableColumn<Booking, String> bookingStatusColumn;
    @FXML private TableColumn<Booking, Void> bookingActionsColumn;
    @FXML private TextField bookingSearchField;

    // Update Room Status Tab
    @FXML private ComboBox<String> roomSelect;
    @FXML private Label currentStatusLabel;
    @FXML private Button btnAvailable;
    @FXML private Button btnOccupied;
    @FXML private Button btnCleaning;
    @FXML private TextArea statusNotesArea;
    @FXML private Button updateStatusButton;

    // Access Logs Tab
    @FXML private TableView<AccessLog> accessLogTable;
    @FXML private TableColumn<AccessLog, String> timestampColumn;
    @FXML private TableColumn<AccessLog, String> logRoomColumn;
    @FXML private TableColumn<AccessLog, String> accessTypeColumn;
    @FXML private TableColumn<AccessLog, String> logCustomerColumn;
    @FXML private TableColumn<AccessLog, String> detailsColumn;
    @FXML private DatePicker logDateFilter;
    @FXML private Button applyFilterButton;

    // Database connection
    private Connection conn;

    // Data collections
    private final ObservableList<Room> allRooms = FXCollections.observableArrayList();
    private final ObservableList<Booking> allBookings = FXCollections.observableArrayList();
    private final ObservableList<AccessLog> allAccessLogs = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            // Assuming utility is a class with a static method to get a database connection
            conn = DatabaseUtil.getConnection();
            System.out.println("Connected to database!");

            // Initialize all components
            setupRoomsTab();
            setupBookingsTab();
            setupUpdateStatusTab();
            setupAccessLogsTab();

            // Load data from the database
            loadDataFromDatabase();

        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            showAlert("Database Error", "Failed to connect to database: " + e.getMessage());
        }
    }

    public void switchToHotel(ActionEvent event) {
        App.switchScene("hoteldashboard.fxml");
    }

    private void setupRoomsTab() {
        // Set up room table columns
        roomNumberColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        roomTypeColumn.setCellValueFactory(new PropertyValueFactory<>("roomType"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        lastCleanedColumn.setCellValueFactory(new PropertyValueFactory<>("lastCleaned"));

        // Custom status cell styling
        statusColumn.setCellFactory(column -> new TableCell<Room, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Available":
                            setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                            break;
                        case "Occupied":
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828; -fx-font-weight: bold;");
                            break;
                        case "Cleaning":
                            setStyle("-fx-background-color: #fff8e1; -fx-text-fill: #f57c00; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });

        // Setup actions column with buttons
        actionsColumn.setCellFactory(createActionButtonCellFactory(
                (room) -> {
                    showAlert("Room Details", "Viewing details for Room " + room.getRoomNumber());
                    // Here you'd typically open a detailed view for the room
                }
        ));

        roomTable.setItems(allRooms);
    }

    private void setupBookingsTab() {
        // Set up booking table columns
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookingId"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        bookingRoomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
        checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
        bookingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Custom booking status cell styling
        bookingStatusColumn.setCellFactory(column -> new TableCell<Booking, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Active":
                            setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #0d47a1; -fx-font-weight: bold;");
                            break;
                        case "Reserved":
                            setStyle("-fx-background-color: #e0f2f1; -fx-text-fill: #004d40; -fx-font-weight: bold;");
                            break;
                        case "Completed":
                            setStyle("-fx-background-color: #f3e5f5; -fx-text-fill: #4a148c; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });

        // Setup actions column for bookings
        bookingActionsColumn.setCellFactory(createActionButtonCellFactory(
                (booking) -> {
                    showAlert("Booking Details", "Viewing details for Booking " + booking.getBookingId());
                    // Here you'd typically open a detailed view for the booking
                }
        ));

        bookingTable.setItems(allBookings);

        // Setup search functionality
        if (bookingSearchField != null) {
            bookingSearchField.setPromptText("Search by name or booking ID...");
            bookingSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterBookings(newValue);
            });
        }
    }

    private void setupUpdateStatusTab() {
        // Initialize room selection combo box (will be populated after loading rooms from DB)
        roomSelect.setOnAction(e -> {
            String selected = roomSelect.getValue();
            if (selected != null) {
                String roomNumber = selected.split(" - ")[0];

                // Find the room's current status
                for (Room room : allRooms) {
                    if (room.getRoomNumber().equals(roomNumber)) {
                        currentStatusLabel.setText("Current Status: " + room.getStatus());

                        // Set style for status label
                        switch (room.getStatus()) {
                            case "Available":
                                currentStatusLabel.setStyle("-fx-text-fill: #2e7d32;");
                                break;
                            case "Occupied":
                                currentStatusLabel.setStyle("-fx-text-fill: #c62828;");
                                break;
                            case "Cleaning":
                                currentStatusLabel.setStyle("-fx-text-fill: #f57c00;");
                                break;
                        }
                        break;
                    }
                }
            }
        });

        // Setup button actions
        btnAvailable.setOnAction(e -> prepareStatusUpdate("Available"));
        btnOccupied.setOnAction(e -> prepareStatusUpdate("Occupied"));
        btnCleaning.setOnAction(e -> prepareStatusUpdate("Cleaning"));

        // Main update button
        updateStatusButton.setOnAction(e -> {
            if (roomSelect.getValue() == null) {
                showAlert("Error", "Please select a room first");
                return;
            }

            String roomNumber = roomSelect.getValue().split(" - ")[0];
            String newStatus = getCurrentSelectedStatus();
            String notes = statusNotesArea.getText();

            updateRoomStatus(roomNumber, newStatus, notes);

            // Clear form
            statusNotesArea.clear();
            showAlert("Success", "Room " + roomNumber + " status updated to " + newStatus);

            // Log this action
            addAccessLog(roomNumber, "Status Change", "Staff",
                    "Status changed to " + newStatus + (notes.isEmpty() ? "" : ": " + notes));
        });
    }

    private void setupAccessLogsTab() {
        // Set up access log table columns
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        logRoomColumn.setCellValueFactory(new PropertyValueFactory<>("roomNumber"));
        accessTypeColumn.setCellValueFactory(new PropertyValueFactory<>("accessType"));
        logCustomerColumn.setCellValueFactory(new PropertyValueFactory<>("person"));
        detailsColumn.setCellValueFactory(new PropertyValueFactory<>("details"));

        accessLogTable.setItems(allAccessLogs);

        // Setup date filter
        if (logDateFilter != null && applyFilterButton != null) {
            logDateFilter.setValue(LocalDate.now());

            applyFilterButton.setOnAction(e -> {
                filterAccessLogs(logDateFilter.getValue());
            });
        }
    }

    private void loadDataFromDatabase() {
        loadRoomsFromDatabase();
        loadBookingsFromDatabase();
        loadAccessLogsFromDatabase();
        populateRoomSelectComboBox();
    }

    private void loadRoomsFromDatabase() {
        String query = "SELECT r.id, r.number, r.status FROM room r";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String roomNumber = rs.getString("number");
                String status = rs.getString("status");

                // For room type, use a derivation based on room number for now
                // In a real system, you'd have a room_type table or field
                String roomType = determineRoomType(roomNumber);

                // For last cleaned, we would ideally have a separate table
                // For now, we'll use a placeholder value
                String lastCleaned = status.equals("Cleaning") ? "In Progress" : "2025-04-18 08:30";

                allRooms.add(new Room(roomNumber, roomType, status, lastCleaned));
            }
        } catch (SQLException e) {
            System.out.println("Error loading rooms: " + e.getMessage());
            showAlert("Database Error", "Failed to load rooms: " + e.getMessage());
        }
    }

    private String determineRoomType(String roomNumber) {
        // This is a placeholder logic - in a real system, this would come from the database
        int num = Integer.parseInt(roomNumber);
        if (num % 3 == 0) return "Suite";
        if (num % 2 == 0) return "Deluxe";
        return "Standard";
    }

    private void loadBookingsFromDatabase() {
        String query = "SELECT b.id, c.name, r.number, b.check_in, b.check_out " +
                "FROM booking b " +
                "JOIN customer c ON b.customer_id = c.id " +
                "JOIN room r ON b.room_id = r.id";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String bookingId = "B" + rs.getInt("id");
                String customerName = rs.getString("name");
                String roomNumber = rs.getString("number");

                // Format dates
                Date checkInDate = rs.getDate("check_in");
                Date checkOutDate = rs.getDate("check_out");
                String checkIn = checkInDate.toString();
                String checkOut = checkOutDate.toString();

                // Determine booking status based on current date and check-in/check-out dates
                String status = determineBookingStatus(checkInDate, checkOutDate);

                allBookings.add(new Booking(bookingId, customerName, roomNumber, checkIn, checkOut, status));
            }
        } catch (SQLException e) {
            System.out.println("Error loading bookings: " + e.getMessage());
            showAlert("Database Error", "Failed to load bookings: " + e.getMessage());
        }
    }

    private String determineBookingStatus(Date checkIn, Date checkOut) {
        Date today = new Date(System.currentTimeMillis());
        if (today.after(checkOut)) {
            return "Completed";
        } else if (today.before(checkIn)) {
            return "Reserved";
        } else {
            return "Active";
        }
    }

    private void loadAccessLogsFromDatabase() {
        String query = "SELECT al.access_time, r.number, c.name, al.id " +
                "FROM accesslog al " +
                "JOIN room r ON al.room_id = r.id " +
                "LEFT JOIN customer c ON al.customer_id = c.id " +
                "ORDER BY al.access_time DESC";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Timestamp accessTime = rs.getTimestamp("access_time");
                String formattedTime = accessTime.toLocalDateTime()
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                String roomNumber = rs.getString("number");

                // Determine if it's a customer or staff access
                String person = rs.getString("name");
                String accessType = person != null ? "Key Card Entry" : "Staff Access";
                person = person != null ? person : "Staff";

                // Generate some details based on the access type
                String details = accessType.equals("Key Card Entry") ? "Guest entry" : "Maintenance check";

                allAccessLogs.add(new AccessLog(formattedTime, roomNumber, accessType, person, details));
            }
        } catch (SQLException e) {
            System.out.println("Error loading access logs: " + e.getMessage());
            showAlert("Database Error", "Failed to load access logs: " + e.getMessage());
        }
    }

    private void populateRoomSelectComboBox() {
        ObservableList<String> roomOptions = FXCollections.observableArrayList();

        for (Room room : allRooms) {
            roomOptions.add(room.getRoomNumber() + " - " + room.getRoomType());
        }

        roomSelect.setItems(roomOptions);
    }

    private String getCurrentSelectedStatus() {
        // This would typically be determined by which button was last pressed
        // For simplicity, we'll just use the current status label text
        String currentText = currentStatusLabel.getText();
        return currentText.replace("Current Status: ", "");
    }

    private void prepareStatusUpdate(String status) {
        if (roomSelect.getValue() != null) {
            currentStatusLabel.setText("Current Status: " + status);

            // Set style for status label
            switch (status) {
                case "Available":
                    currentStatusLabel.setStyle("-fx-text-fill: #2e7d32;");
                    break;
                case "Occupied":
                    currentStatusLabel.setStyle("-fx-text-fill: #c62828;");
                    break;
                case "Cleaning":
                    currentStatusLabel.setStyle("-fx-text-fill: #f57c00;");
                    break;
            }
        } else {
            showAlert("Error", "Please select a room first");
        }
    }

    private void updateRoomStatus(String roomNumber, String newStatus, String notes) {
        // Update the room status in the database
        String query = "UPDATE room SET status = ? WHERE number = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newStatus);
            stmt.setString(2, roomNumber);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Update the room in our collection
                for (Room room : allRooms) {
                    if (room.getRoomNumber().equals(roomNumber)) {
                        room.setStatus(newStatus);

                        // Update lastCleaned if status is Available (assuming cleaning is done)
                        if (newStatus.equals("Available")) {
                            room.setLastCleaned(getCurrentTimestamp());
                        } else if (newStatus.equals("Cleaning")) {
                            room.setLastCleaned("In Progress");
                        }

                        break;
                    }
                }

                // Refresh the table
                roomTable.refresh();
            } else {
                showAlert("Error", "Failed to update room status. Room not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error updating room status: " + e.getMessage());
            showAlert("Database Error", "Failed to update room status: " + e.getMessage());
        }
    }

    private void addAccessLog(String roomNumber, String accessType, String person, String details) {
        // First, get the room_id from the room number
        int roomId = getRoomIdFromNumber(roomNumber);

        if (roomId == -1) {
            System.out.println("Room not found: " + roomNumber);
            return;
        }

        // Add the log to the database
        String query = "INSERT INTO accesslog (access_time, room_id, customer_id) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(2, roomId);

            // For simplicity, we're setting customer_id to NULL for staff access
            if (person.equals("Staff")) {
                stmt.setNull(3, java.sql.Types.INTEGER);
            } else {
                // In a real app, you'd look up the customer ID based on the name
                // For now, we'll use a placeholder value
                stmt.setInt(3, 1);  // Assuming customer ID 1 exists
            }

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Create a new log entry for the UI
                AccessLog log = new AccessLog(
                        getCurrentTimestamp(),
                        roomNumber,
                        accessType,
                        person,
                        details
                );

                // Add to our collection
                allAccessLogs.add(0, log); // Add at the top
                accessLogTable.refresh();
            }

        } catch (SQLException e) {
            System.out.println("Error adding access log: " + e.getMessage());
            showAlert("Database Error", "Failed to add access log: " + e.getMessage());
        }
    }

    private int getRoomIdFromNumber(String roomNumber) {
        String query = "SELECT id FROM room WHERE number = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, roomNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting room ID: " + e.getMessage());
        }

        return -1;  // Room not found
    }

    private void filterBookings(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            bookingTable.setItems(allBookings);
            return;
        }

        // Filter bookings based on search text
        ObservableList<Booking> filteredList = FXCollections.observableArrayList();
        String lowerCaseFilter = searchText.toLowerCase();

        for (Booking booking : allBookings) {
            if (booking.getCustomerName().toLowerCase().contains(lowerCaseFilter) ||
                    booking.getBookingId().toLowerCase().contains(lowerCaseFilter)) {
                filteredList.add(booking);
            }
        }

        bookingTable.setItems(filteredList);
    }

    private void filterAccessLogs(LocalDate date) {
        if (date == null) {
            accessLogTable.setItems(allAccessLogs);
            return;
        }

        // Filter logs based on date
        ObservableList<AccessLog> filteredList = FXCollections.observableArrayList();
        String datePrefix = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        for (AccessLog log : allAccessLogs) {
            if (log.getTimestamp().startsWith(datePrefix)) {
                filteredList.add(log);
            }
        }

        accessLogTable.setItems(filteredList);
    }

    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private <T> Callback<TableColumn<T, Void>, TableCell<T, Void>> createActionButtonCellFactory(
            java.util.function.Consumer<T> action) {
        return new Callback<>() {
            @Override
            public TableCell<T, Void> call(final TableColumn<T, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Details");

                    {
                        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
                        btn.setOnAction(event -> {
                            T data = getTableView().getItems().get(getIndex());
                            action.accept(data);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
            }
        };
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Model classes (unchanged)
    public static class Room {
        private final String roomNumber;
        private final String roomType;
        private String status;
        private String lastCleaned;

        public Room(String roomNumber, String roomType, String status, String lastCleaned) {
            this.roomNumber = roomNumber;
            this.roomType = roomType;
            this.status = status;
            this.lastCleaned = lastCleaned;
        }

        public String getRoomNumber() { return roomNumber; }
        public String getRoomType() { return roomType; }
        public String getStatus() { return status; }
        public String getLastCleaned() { return lastCleaned; }

        public void setStatus(String status) { this.status = status; }
        public void setLastCleaned(String lastCleaned) { this.lastCleaned = lastCleaned; }
    }

    public static class Booking {
        private final String bookingId;
        private final String customerName;
        private final String roomNumber;
        private final String checkIn;
        private final String checkOut;
        private final String status;

        public Booking(String bookingId, String customerName, String roomNumber,
                       String checkIn, String checkOut, String status) {
            this.bookingId = bookingId;
            this.customerName = customerName;
            this.roomNumber = roomNumber;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.status = status;
        }

        public String getBookingId() { return bookingId; }
        public String getCustomerName() { return customerName; }
        public String getRoomNumber() { return roomNumber; }
        public String getCheckIn() { return checkIn; }
        public String getCheckOut() { return checkOut; }
        public String getStatus() { return status; }
    }

    public static class AccessLog {
        private final String timestamp;
        private final String roomNumber;
        private final String accessType;
        private final String person;
        private final String details;

        public AccessLog(String timestamp, String roomNumber, String accessType,
                         String person, String details) {
            this.timestamp = timestamp;
            this.roomNumber = roomNumber;
            this.accessType = accessType;
            this.person = person;
            this.details = details;
        }

        public String getTimestamp() { return timestamp; }
        public String getRoomNumber() { return roomNumber; }
        public String getAccessType() { return accessType; }
        public String getPerson() { return person; }
        public String getDetails() { return details; }
    }
}