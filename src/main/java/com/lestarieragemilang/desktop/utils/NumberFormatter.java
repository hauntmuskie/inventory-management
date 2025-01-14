package com.lestarieragemilang.desktop.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

/**
 * Utility class for formatting numbers in the Indonesian locale format.
 * Provides methods to format numbers with thousands separators and handle text field input validation.
 */
public final class NumberFormatter {
    private static final DecimalFormat formatter;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("id", "ID"));
        formatter = new DecimalFormat("#,###", symbols);
        formatter.setGroupingSize(3);
    }

    /**
     * Private constructor to prevent instantiation of utility class.
     * @throws AssertionError if instantiation is attempted
     */
    private NumberFormatter() {
        throw new AssertionError("No instances");
    }

    /**
     * Applies number formatting to a TextField with automatic thousand separators.
     * Only allows numeric input and automatically formats the number as it's typed.
     *
     * @param textField the TextField to apply formatting to
     * @throws NullPointerException if textField is null
     */
    public static void applyNumberFormat(TextField textField) {
        Preconditions.checkNotNull(textField, "TextField must not be null");

        textField.textProperty().addListener((_, oldValue, newValue) -> {
            if (Strings.isNullOrEmpty(newValue)) {
                return;
            }

            if (!newValue.matches("[0-9.,]*")) {
                textField.setText(oldValue);
                return;
            }

            try {
                String digitsOnly = newValue.replaceAll("[^0-9]", "");
                
                if (Strings.isNullOrEmpty(digitsOnly)) {
                    textField.setText("");
                    return;
                }

                long number = Long.parseLong(digitsOnly);
                updateTextFieldValue(textField, number, oldValue);
                
            } catch (NumberFormatException e) {
                textField.setText(Strings.nullToEmpty(oldValue));
            }
        });
    }

    /**
     * Updates the TextField value with proper formatting and maintains cursor position.
     *
     * @param textField the TextField to update
     * @param number the numeric value to format
     * @param oldValue the previous value for cursor position calculation
     */
    private static void updateTextFieldValue(TextField textField, long number, String oldValue) {
        int caretPosition = textField.getCaretPosition();
        int oldLength = textField.getText().length();
        
        String formatted = formatter.format(number);
        textField.setText(formatted);
        
        int newPosition = Math.min(
            Math.max(0, caretPosition + (formatted.length() - oldLength)),
            formatted.length()
        );
        textField.positionCaret(newPosition);
    }

    /**
     * Extracts the numeric value from a formatted string.
     *
     * @param formattedText the formatted text to parse
     * @return the numeric value as a string, returns "0" if parsing fails
     */
    public static String getNumericValue(String formattedText) {
        if (Strings.isNullOrEmpty(formattedText)) {
            return "0";
        }
        try {
            Number number = formatter.parse(formattedText);
            return String.valueOf(number.longValue());
        } catch (ParseException e) {
            return "0";
        }
    }

    /**
     * Formats a BigDecimal value using the Indonesian number format.
     *
     * @param value the BigDecimal to format
     * @return the formatted string with thousand separators
     * @throws NullPointerException if value is null
     */
    public static String formatValue(BigDecimal value) {
        Preconditions.checkNotNull(value, "Value must not be null");
        return formatter.format(value);
    }
}
