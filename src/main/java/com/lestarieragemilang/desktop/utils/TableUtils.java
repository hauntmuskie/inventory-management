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

public class TableUtils {

    private static final Locale DEFAULT_LOCALE = new Locale.Builder().setLanguage("id").setRegion("ID").build();
    private static final ThreadLocal<NumberFormat> CURRENCY_FORMAT = ThreadLocal
            .withInitial(() -> NumberFormat.getCurrencyInstance(DEFAULT_LOCALE));

    private static final Cache<String, Method> METHOD_CACHE = CacheBuilder.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    public static void setLocale(Locale newLocale) {
        CURRENCY_FORMAT.set(NumberFormat.getCurrencyInstance(checkNotNull(newLocale)));
    }

    public static <T> void populateTable(TableView<T> tableView, List<TableColumn<T, ?>> columns, List<T> data) {
        checkNotNull(tableView).getColumns().setAll(checkNotNull(columns));
        tableView.setItems(FXCollections.observableArrayList(checkNotNull(data)));
    }

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

    public static String formatCurrency(BigDecimal value) {
        return CURRENCY_FORMAT.get().format(checkNotNull(value));
    }

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