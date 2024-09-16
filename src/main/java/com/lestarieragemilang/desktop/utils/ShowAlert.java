package com.lestarieragemilang.desktop.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class ShowAlert {

    @SuppressWarnings("unused")
    public static void showAlert(AlertType alertType, String title, String headerText, String... messages) {
        String message = buildMessage(messages);
        createAlert(alertType, title, headerText, message);
    }

    private static String buildMessage(String... messages) {
        StringBuilder messageBuilder = new StringBuilder();
        for (String message : messages) {
            messageBuilder.append(message).append("\n");
        }
        return messageBuilder.toString().trim();
    }

    private static void createAlert(AlertType alertType, String title, String headerText, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String headerText, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
}