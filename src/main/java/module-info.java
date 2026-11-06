module cda.vehicules.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.fasterxml.jackson.databind;
    requires org.apache.logging.log4j;
    requires jbcrypt;
    requires sib.api.v3.sdk;
    requires io;
    requires kernel;
    requires layout;


    opens cda.vehicules to javafx.fxml;
    exports cda.vehicules;
}