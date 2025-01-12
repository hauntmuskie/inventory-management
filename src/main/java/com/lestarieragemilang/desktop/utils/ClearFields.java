package com.lestarieragemilang.desktop.utils;

import com.google.common.base.Preconditions;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * Utility class for clearing JavaFX form fields.
 */
public class ClearFields {

    /**
     * Clears the values of multiple JavaFX form fields.
     * Currently supports TextField and ComboBox components.
     *
     * @param fields Variable number of form fields to be cleared
     * @throws NullPointerException if the fields array or any field is null
     * @throws IllegalArgumentException if an unsupported field type is provided
     */
    public static void clearFields(Object... fields) {
        Preconditions.checkNotNull(fields, "Fields array cannot be null");

        for (Object field : fields) {
            Preconditions.checkNotNull(field, "Field cannot be null");

            if (field instanceof TextField) {
                ((TextField) field).clear();
            } else if (field instanceof ComboBox<?>) {
                ((ComboBox<?>) field).setValue(null);
            } else {
                throw new IllegalArgumentException("Unsupported field type: " + field.getClass().getName());
            }
        }
    }
}