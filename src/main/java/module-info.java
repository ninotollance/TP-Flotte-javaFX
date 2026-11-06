module cda.vehicules.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens cda.vehicules.demo to javafx.fxml;
    exports cda.vehicules.demo;
}