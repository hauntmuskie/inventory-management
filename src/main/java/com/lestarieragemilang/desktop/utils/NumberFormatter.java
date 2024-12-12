package com.lestarieragemilang.desktop.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;

public final class NumberFormatter {
    private static final DecimalFormat formatter;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.of("id", "ID"));
        formatter = new DecimalFormat("#,###", symbols);
        formatter.setGroupingSize(3);
    }

    private NumberFormatter() {
        throw new AssertionError("No instances");
    }

    public static void applyNumberFormat(TextField textField) {
        Preconditions.checkNotNull(textField, "TextField must not be null");

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
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

    public static String formatValue(BigDecimal value) {
        Preconditions.checkNotNull(value, "Value must not be null");
        return formatter.format(value);
    }
}
