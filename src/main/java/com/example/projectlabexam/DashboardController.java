package com.example.projectlabexam;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

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

        if (selectedRoom == null || checkIn == null || checkOut == null) {
            bookingDetails.setText("Please fill in all fields.");
            return;
        }

        if (!checkIn.isBefore(checkOut)) {
            bookingDetails.setText("Check-in must be before check-out.");
            return;
        }

        currentBooking = new Booking(selectedRoom, checkIn, checkOut);
        bookingDetails.setText("Booked " + selectedRoom + " from " + checkIn + " to " + checkOut);
        updateKeycardStatus();
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
}