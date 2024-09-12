package com.lestarieragemilang.desktop.utils;

import javafx.scene.control.Alert;

public class ShowAlert {

    @SuppressWarnings("unused")
    public static void showAlert(String... messages) {
        StringBuilder messageBuilder = new StringBuilder();
        for (String message : messages) {
            messageBuilder.append(message).append("\n");
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(messageBuilder.toString().trim());
        alert.showAndWait();
    }
}
