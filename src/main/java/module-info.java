module com.jacobwigent.wiretap {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.fazecast.jSerialComm;

    opens com.jacobwigent.wiretap to javafx.fxml;
    exports com.jacobwigent.wiretap;
}