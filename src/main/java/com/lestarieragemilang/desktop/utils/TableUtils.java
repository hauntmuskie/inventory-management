package com.lestarieragemilang.desktop.utils;

import com.google.common.base.CaseFormat;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility class for JavaFX TableView operations.
 * Provides methods for creating and populating table columns with formatted data.
 */
public class TableUtils {

    private static final Locale DEFAULT_LOCALE = new Locale.Builder().setLanguage("id").setRegion("ID").build();
    private static final ThreadLocal<NumberFormat> CURRENCY_FORMAT = ThreadLocal
            .withInitial(() -> NumberFormat.getCurrencyInstance(DEFAULT_LOCALE));

    private static final Cache<String, Method> METHOD_CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    /**
     * Sets the locale for currency formatting in table cells.
     * @param newLocale The locale to use for formatting
     */
    public static void setLocale(Locale newLocale) {
        CURRENCY_FORMAT.set(NumberFormat.getCurrencyInstance(checkNotNull(newLocale)));
    }

    /**
     * Populates a TableView with columns and data.
     * @param tableView The TableView to populate
     * @param columns List of columns to add
     * @param data List of data items to display
     * @param <T> The type of the data items
     */
    public static <T> void populateTable(TableView<T> tableView, List<TableColumn<T, ?>> columns, List<T> data) {
        checkNotNull(tableView).getColumns().setAll(checkNotNull(columns));
        tableView.setItems(FXCollections.observableArrayList(checkNotNull(data)));
    }

    /**
     * Creates a table column for displaying string values.
     * @param title The column header text
     * @param property The property path to get the value (supports nested properties)
     * @param <T> The type of the data items
     * @return A configured TableColumn
     */
    public static <T> TableColumn<T, String> createColumn(String title, String property) {
        checkNotNull(title);
        checkNotNull(property);

        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> {
            try {
                Object value = getPropertyValue(cellData.getValue(), property);
                return new SimpleStringProperty(value != null ? value.toString() : "");
            } catch (Exception e) {
                return new SimpleStringProperty("");
            }
        });
        return column;
    }

    /**
     * Creates a table column for displaying formatted currency values.
     * @param title The column header text
     * @param property The property path to get the BigDecimal value
     * @param <T> The type of the data items
     * @return A configured TableColumn with currency formatting
     */
    public static <T> TableColumn<T, BigDecimal> createFormattedColumn(String title, String property) {
        checkNotNull(title);
        checkNotNull(property);

        TableColumn<T, BigDecimal> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> {
            try {
                Object value = getPropertyValue(cellData.getValue(), property);
                return new SimpleObjectProperty<>(value instanceof BigDecimal ? (BigDecimal) value : null);
            } catch (Exception e) {
                return new SimpleObjectProperty<>(null);
            }
        });

        column.setCellFactory(_ -> new TableCell<T, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : CURRENCY_FORMAT.get().format(item));
            }
        });
        return column;
    }

    /**
     * Formats a BigDecimal value as currency using the current locale.
     * @param value The value to format
     * @return The formatted currency string
     */
    public static String formatCurrency(BigDecimal value) {
        return CURRENCY_FORMAT.get().format(checkNotNull(value));
    }

    /**
     * Gets a property value from an object using reflection.
     * Supports nested properties using dot notation.
     * @param object The source object
     * @param property The property path (e.g. "category.name")
     * @return The property value
     */
    private static Object getPropertyValue(Object object, String property) throws Exception {
        checkNotNull(object);
        checkNotNull(property);

        ImmutableList<String> properties = ImmutableList.copyOf(property.split("\\."));
        Object result = object;

        for (String prop : properties) {
            if (result == null) break;
            Method method = getMethod(result.getClass(), "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, prop));
            result = method.invoke(result);
        }

        return result;
    }

    private static Method getMethod(Class<?> clazz, String methodName) throws Exception {
        String key = clazz.getName() + "#" + methodName;
        Method method = METHOD_CACHE.getIfPresent(key);
        
        if (method == null) {
            method = clazz.getMethod(methodName);
            METHOD_CACHE.put(key, method);
        }
        
        return method;
    }
}