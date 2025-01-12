package com.lestarieragemilang.desktop.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import java.util.Optional;

public class ShowAlert {
    private static final Joiner LINE_JOINER = Joiner.on("\n").skipNulls();

    /**
     * Centers an alert dialog relative to its owner window.
     * @param alert The alert dialog to be centered
     */
    private static void centerAlert(Alert alert) {
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.setOnShowing(_ -> {
            Stage owner = (Stage) stage.getOwner();
            if (owner != null) {
                stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
                stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
            }
        });
    }

    /**
     * Displays a customizable alert dialog.
     * @param alertType The type of alert to show (ERROR, WARNING, INFORMATION, etc.)
     * @param title The title of the alert dialog
     * @param headerText The header text (can be null)
     * @param messages Variable number of messages to be displayed in the content area
     * @throws NullPointerException if alertType or messages is null
     * @throws IllegalArgumentException if title is null or empty
     */
    public static void showAlert(AlertType alertType, String title, String headerText, String... messages) {
        Preconditions.checkNotNull(alertType, "AlertType tidak boleh kosong");
        Preconditions.checkNotNull(messages, "Pesan tidak boleh kosong");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title), "Judul tidak boleh kosong");
        
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(LINE_JOINER.join(messages));
        
        centerAlert(alert);
        alert.showAndWait();
    }

    /**
     * Displays a confirmation dialog and returns the user's choice.
     * @param title The title of the confirmation dialog
     * @param headerText The header text (can be null)
     * @param content The content text to display
     * @return true if user clicks OK, false otherwise
     * @throws IllegalArgumentException if title or content is null or empty
     */
    public static boolean showConfirmation(String title, String headerText, String content) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title), "Judul tidak boleh kosong");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(content), "Konten tidak boleh kosong");
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);

        centerAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.filter(buttonType -> buttonType == ButtonType.OK).isPresent();
    }

    /**
     * Displays an information alert with the specified message.
     * @param message The message to display
     * @throws IllegalArgumentException if message is null or empty
     */
    public static void showInfo(String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan tidak boleh kosong");
        Alert alert = new Alert(AlertType.INFORMATION, message);
        alert.setTitle("Informasi");
        centerAlert(alert);
        alert.showAndWait();
    }

    /**
     * Displays an error alert with the specified message.
     * @param message The error message to display
     * @throws IllegalArgumentException if message is null or empty
     */
    public static void showError(String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan error tidak boleh kosong");
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.setTitle("Error");
        centerAlert(alert);
        alert.showAndWait();
    }

    /**
     * Displays a warning alert with the specified message.
     * @param message The warning message to display
     * @throws IllegalArgumentException if message is null or empty
     */
    public static void showWarning(String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan peringatan tidak boleh kosong");
        Alert alert = new Alert(AlertType.WARNING, message);
        alert.setTitle("Peringatan");
        centerAlert(alert);
        alert.showAndWait();
    }

    /**
     * Displays a Yes/No confirmation dialog.
     * @param title The title of the dialog
     * @param message The message to display
     * @return true if user clicks Yes, false otherwise
     * @throws IllegalArgumentException if title or message is null or empty
     */
    public static boolean showYesNo(String title, String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title), "Judul tidak boleh kosong");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan tidak boleh kosong");
        return showConfirmation(title, null, message);
    }

    /**
     * Displays a success message using an information alert.
     * @param message The success message to display
     * @throws IllegalArgumentException if message is null or empty
     */
    public static void showSuccess(String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan sukses tidak boleh kosong");
        Alert alert = new Alert(AlertType.INFORMATION, message);
        alert.setTitle("Sukses");
        centerAlert(alert);
        alert.showAndWait();
    }

    /**
     * Displays a validation error message using an error alert.
     * @param message The validation error message to display
     * @throws IllegalArgumentException if message is null or empty
     */
    public static void showValidationError(String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan validasi error tidak boleh kosong");
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.setTitle("Validasi Error");
        centerAlert(alert);
        alert.showAndWait();
    }

    /**
     * Displays a database error message using an error alert.
     * @param message The database error message to display
     * @throws IllegalArgumentException if message is null or empty
     */
    public static void showDatabaseError(String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan database error tidak boleh kosong");
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.setTitle("Database Error");
        centerAlert(alert);
        alert.showAndWait();
    }
}