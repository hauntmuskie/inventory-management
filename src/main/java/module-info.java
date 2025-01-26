module com.lestarieragemilang.desktop {
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires transitive org.hibernate.orm.core;
    requires transitive org.jfxtras.styles.jmetro;
    requires javafx.base;
    requires jasperreports;
    requires mysql.connector.j;
    requires jsr305;
    requires java.desktop;
    requires java.base;
    requires javafx.fxml;
    requires jakarta.persistence;
    requires java.sql;
    requires animatefx;
    requires com.jfoenix;
    requires java.naming;
    requires java.net.http;
    requires javafx.web;
    requires org.slf4j;
    requires com.google.common;
    requires jbcrypt;
    requires com.zaxxer.hikari;
    requires java.prefs;

    opens com.lestarieragemilang.desktop to javafx.fxml;
    opens com.lestarieragemilang.desktop.controller to javafx.fxml;
    opens com.lestarieragemilang.desktop.controller.report to javafx.fxml;
    opens com.lestarieragemilang.desktop.model to org.hibernate.orm.core, javafx.base;
    opens com.lestarieragemilang.desktop.utils to javafx.graphics;

    exports com.lestarieragemilang.desktop;
    exports com.lestarieragemilang.desktop.controller;
    exports com.lestarieragemilang.desktop.controller.report;
    exports com.lestarieragemilang.desktop.utils;
}
