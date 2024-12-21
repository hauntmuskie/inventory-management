package com.lestarieragemilang.desktop.utils;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class ClearFields {

    public static void clearFields(Object... fields) {
        for (Object field : fields) {
            if (field instanceof TextField) {
                ((TextField) field).clear();
            } else if (field instanceof ComboBox<?>) {
                ((ComboBox<?>) field).setValue(null);
            }
        }
    }
}