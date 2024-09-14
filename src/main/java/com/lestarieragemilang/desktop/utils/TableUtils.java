package com.lestarieragemilang.desktop.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.List;

public class TableUtils {

    public static <T> void populateTable(TableView<T> tableView, List<TableColumn<T, ?>> columns, List<T> data) {
        tableView.getColumns().setAll(columns);
        tableView.setItems(FXCollections.observableArrayList(data));
    }

    public static <T> TableColumn<T, String> createColumn(String title, String property) {
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<T, String>, javafx.beans.value.ObservableValue<String>>() {
                    @Override
                    public javafx.beans.value.ObservableValue<String> call(TableColumn.CellDataFeatures<T, String> param) {
                        try {
                            String[] properties = property.split("\\.");
                            Object value = param.getValue();
                            for (String prop : properties) {
                                value = value.getClass().getMethod("get" + capitalize(prop)).invoke(value);
                            }
                            return new SimpleStringProperty(value != null ? value.toString() : "");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                });
        return column;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}