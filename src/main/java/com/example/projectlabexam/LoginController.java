package com.example.projectlabexam;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label messageLabel;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Customer", "Staff");
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        if (email.isEmpty() || password.isEmpty() || role == null) {
            messageLabel.setText("Please fill all fields");
        } else {
            messageLabel.setText("Welcome " + role + "!");
            // You can add authentication logic here
            // Example: switch to dashboard based on role
            if ("Customer".equals(role)) {
                App.switchScene("hoteldashboard.fxml");
            } else if ("Staff".equals(role)) {
                App.switchScene("staffdashboard.fxml");
            }
        }
    }
}