module com.example.otelrezervasyonsistemi {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.desktop;

    opens com.example.otelrezervasyonsistemi to javafx.fxml;
    exports com.example.otelrezervasyonsistemi;
}