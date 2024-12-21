package com.lestarieragemilang.desktop.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class ShowAlert {
    private static final Joiner LINE_JOINER = Joiner.on("\n").skipNulls();

    public static void showAlert(AlertType alertType, String title, String headerText, String... messages) {
        Preconditions.checkNotNull(alertType, "AlertType tidak boleh kosong");
        Preconditions.checkNotNull(messages, "Pesan tidak boleh kosong");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title), "Judul tidak boleh kosong");
        
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(LINE_JOINER.join(messages));
        
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String headerText, String content) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title), "Judul tidak boleh kosong");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(content), "Konten tidak boleh kosong");
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.filter(buttonType -> buttonType == ButtonType.OK).isPresent();
    }

    public static void showInfo(String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan tidak boleh kosong");
        showAlert(AlertType.INFORMATION, "Informasi", null, message);
    }

    public static void showError(String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan error tidak boleh kosong");
        showAlert(AlertType.ERROR, "Error", null, message);
    }

    public static void showWarning(String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan peringatan tidak boleh kosong");
        showAlert(AlertType.WARNING, "Peringatan", null, message);
    }

    public static boolean showYesNo(String title, String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title), "Judul tidak boleh kosong");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan tidak boleh kosong");
        return showConfirmation(title, null, message);
    }

    public static void showSuccess(String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan sukses tidak boleh kosong");
        showAlert(AlertType.INFORMATION, "Sukses", null, message);
    }

    public static void showValidationError(String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan validasi error tidak boleh kosong");
        showAlert(AlertType.ERROR, "Validasi Error", null, message);
    }

    public static void showDatabaseError(String message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(message), "Pesan database error tidak boleh kosong");
        showAlert(AlertType.ERROR, "Database Error", null, message);
    }
}