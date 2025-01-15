package com.lestarieragemilang.desktop.utils;

import com.google.common.base.Preconditions;
import com.jfoenix.controls.JFXTextArea;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;

public class ClearFields {
    public static void clearFields(Object... fields) {
        Preconditions.checkNotNull(fields, "Fields array cannot be null");

        for (Object field : fields) {
            Preconditions.checkNotNull(field, "Field cannot be null");

            if (field instanceof TextField) {
                ((TextField) field).clear();
            } else if (field instanceof ComboBox<?>) {
                ((ComboBox<?>) field).setValue(null);
            } else if (field instanceof DatePicker) {
                ((DatePicker) field).setValue(null);
            } else if (field instanceof TextArea || field instanceof JFXTextArea) {
                ((TextArea) field).clear();
            } else {
                throw new IllegalArgumentException("Unsupported field type: " + field.getClass().getName());
            }
        }
    }
}