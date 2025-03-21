module com.example.window {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.window to javafx.fxml;
    exports com.example.window;
}