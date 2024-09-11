module com.lestarieragemilang.desktop {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.lestarieragemilang.desktop to javafx.fxml;
    exports com.lestarieragemilang.desktop;
}
