module com.jacobwigent.wiretap {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fazecast.jSerialComm;
    requires java.desktop;

    opens com.jacobwigent.wiretap to javafx.fxml;
    exports com.jacobwigent.wiretap;
    exports com.jacobwigent.wiretap.display;
    opens com.jacobwigent.wiretap.display to javafx.fxml;
    exports com.jacobwigent.wiretap.serial;
    opens com.jacobwigent.wiretap.serial to javafx.fxml;
}