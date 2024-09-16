module com.lestarieragemilang.desktop {
    requires transitive javafx.graphics;
    requires transitive mysql.connector.j;
    requires java.dotenv;
    requires java.desktop;
    requires java.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires animatefx;
    requires org.jfxtras.styles.jmetro;
    requires com.jfoenix;
    requires java.naming;
    requires java.net.http;
    requires com.google.gson;
    requires org.commonmark;
    requires javafx.web;
    requires org.slf4j;

    opens com.lestarieragemilang.desktop to javafx.fxml;
    opens com.lestarieragemilang.desktop.controller to javafx.fxml;
    opens com.lestarieragemilang.desktop.model to org.hibernate.orm.core, javafx.base;

    exports com.lestarieragemilang.desktop;
    exports com.lestarieragemilang.desktop.controller;
}
