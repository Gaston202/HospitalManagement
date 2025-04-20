module com.example.projectlabexam {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.desktop;
    requires java.sql;

    opens com.example.projectlabexam to javafx.fxml;
    exports com.example.projectlabexam;
}