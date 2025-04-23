package com.example.projectlabexam;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.event.ActionEvent;

public class DashboardController {

    @FXML
    private ComboBox<String> roomComboBox;

    @FXML
    private DatePicker checkInDate;

    @FXML
    private DatePicker checkOutDate;

    @FXML
    private Label bookingDetails;

    @FXML
    private Button keycardButton;

    private Booking currentBooking;

    @FXML
    public void initialize() {
        roomComboBox.getItems().addAll("Room 101", "Room 102", "Room 201", "Room 202");
    }

    @FXML
    private void handleBooking() {
        String selectedRoom = roomComboBox.getValue();
        LocalDate checkIn = checkInDate.getValue();
        LocalDate checkOut = checkOutDate.getValue();

        // Input validation
        if (selectedRoom == null || checkIn == null || checkOut == null) {
            bookingDetails.setText("Please fill in all fields.");
            return;
        }
        if (checkIn.isBefore(LocalDate.now())) {
            bookingDetails.setText("Check-in date must be today or in the future.");
            return;
        }
        if (!checkIn.isBefore(checkOut)) {
            bookingDetails.setText("Check-in must be before check-out.");
            return;
        }
        if (!isRoomAvailable(selectedRoom, checkIn, checkOut)) {
            bookingDetails.setText("Room is not available for the selected dates.");
            return;
        }

        boolean success = saveBookingToDatabase(selectedRoom, checkIn, checkOut);
        if (success) {
            currentBooking = new Booking(selectedRoom, checkIn, checkOut);
            bookingDetails.setText("Booked " + selectedRoom + " from " + checkIn + " to " + checkOut);
        } else {
            bookingDetails.setText("Failed to book room. Please try again.");
        }
        updateKeycardStatus();
    }

    // Check if the room is available for the selected dates
    private boolean isRoomAvailable(String room, LocalDate checkIn, LocalDate checkOut) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String roomNumber = room.replace("Room ", "");
            int roomId = -1;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM room WHERE number = ?")) {
                stmt.setString(1, roomNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        roomId = rs.getInt("id");
                    }
                }
            }
            if (roomId == -1) return false;
            String sql = "SELECT COUNT(*) FROM booking WHERE room_id = ? AND (check_in < ? AND check_out > ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, roomId);
                stmt.setDate(2, java.sql.Date.valueOf(checkOut));
                stmt.setDate(3, java.sql.Date.valueOf(checkIn));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) == 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean saveBookingToDatabase(String room, LocalDate checkIn, LocalDate checkOut) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Find room ID by room number (assuming roomComboBox items are like "Room 101")
            String roomNumber = room.replace("Room ", "");
            int roomId = -1;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM room WHERE number = ?")) {
                stmt.setString(1, roomNumber);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        roomId = rs.getInt("id");
                    }
                }
            }
            if (roomId == -1) return false;

            // For demo, use a default customer ID (e.g., 1)
            int customerId = 1;

            String sql = "INSERT INTO booking (customer_id, room_id, check_in, check_out) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, customerId);
                stmt.setInt(2, roomId);
                stmt.setDate(3, java.sql.Date.valueOf(checkIn));
                stmt.setDate(4, java.sql.Date.valueOf(checkOut));
                int rows = stmt.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleKeycardUse() {
        if (currentBooking != null && currentBooking.isValid()) {
            bookingDetails.setText("Keycard access granted for " + currentBooking.getRoom());
        } else {
            bookingDetails.setText("No valid booking for keycard access.");
        }
    }
    public void switchToStaff(ActionEvent event) {
        App.switchScene("staffdashboard.fxml");
    }
    private void updateKeycardStatus() {
        keycardButton.setDisable(currentBooking == null || !currentBooking.isValid());
    }

    @FXML
    private void backToLogin() {
        App.switchScene("login.fxml");
    }
}