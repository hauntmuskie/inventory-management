module com.lestarieragemilang.desktop {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires animatefx;
    requires org.jfxtras.styles.jmetro;
    requires com.jfoenix;
    requires java.naming;

    opens com.lestarieragemilang.desktop to javafx.fxml;
    opens com.lestarieragemilang.desktop.controller to javafx.fxml;
    opens com.lestarieragemilang.desktop.model to org.hibernate.orm.core, javafx.base;


    exports com.lestarieragemilang.desktop;
}
