package com.example.projectlabexam;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static Stage primaryStage;
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        switchScene("staffdashboard.fxml");
        primaryStage.show();
    }

    public static void switchScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml));
            Parent root = loader.load();
            scene = new Scene(root, 640, 480);
            primaryStage.setScene(scene);
            primaryStage.setTitle(fxml.contains("staff") ? "Staff Dashboard" : "Hotel Dashboard");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}
