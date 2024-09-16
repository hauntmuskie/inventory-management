package com.lestarieragemilang.desktop.utils;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TableUtils {

    private static final Locale DEFAULT_LOCALE = new Locale.Builder().setLanguage("id").setRegion("ID").build();
    private static final ThreadLocal<NumberFormat> CURRENCY_FORMAT = ThreadLocal
            .withInitial(() -> NumberFormat.getCurrencyInstance(DEFAULT_LOCALE));

    private static final Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

    public static void setLocale(Locale newLocale) {
        CURRENCY_FORMAT.set(NumberFormat.getCurrencyInstance(newLocale));
    }

    public static <T> void populateTable(TableView<T> tableView, List<TableColumn<T, ?>> columns, List<T> data) {
        tableView.getColumns().setAll(columns);
        tableView.setItems(FXCollections.observableArrayList(data));
    }

    public static <T> TableColumn<T, String> createColumn(String title, String property) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> {
            try {
                Object value = getPropertyValue(cellData.getValue(), property);
                return new SimpleStringProperty(value != null ? value.toString() : "");
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleStringProperty("");
            }
        });
        return column;
    }

    public static <T> TableColumn<T, BigDecimal> createFormattedColumn(String title, String property) {
        TableColumn<T, BigDecimal> column = new TableColumn<>(title);
        column.setCellValueFactory(cellData -> {
            try {
                Object value = getPropertyValue(cellData.getValue(), property);
                return new SimpleObjectProperty<>(value instanceof BigDecimal ? (BigDecimal) value : null);
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleObjectProperty<>(null);
            }
        });

        column.setCellFactory(col -> new TableCell<T, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    try {
                        setText(CURRENCY_FORMAT.get().format(item));
                    } catch (IllegalArgumentException e) {
                        setText("N/A");
                        e.printStackTrace();
                    }
                }
            }
        });
        return column;
    }

    private static Object getPropertyValue(Object object, String property) throws Exception {
        String[] properties = property.split("\\.");
        for (String prop : properties) {
            object = getMethod(object.getClass(), "get" + capitalize(prop)).invoke(object);
            if (object == null) {
                break;
            }
        }
        return object;
    }

    private static Method getMethod(Class<?> clazz, String methodName) throws NoSuchMethodException {
        String key = clazz.getName() + "#" + methodName;
        return METHOD_CACHE.computeIfAbsent(key, k -> {
            try {
                return clazz.getMethod(methodName);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static String capitalize(String str) {
        return str.isEmpty() ? str : Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}