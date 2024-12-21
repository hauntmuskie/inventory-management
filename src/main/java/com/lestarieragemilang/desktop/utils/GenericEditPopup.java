package com.lestarieragemilang.desktop.utils;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class GenericEditPopup<T> {

    private String title;
    private T item;
    private List<FieldConfig<T>> fieldConfigs;
    private BiConsumer<T, List<Control>> saveAction;
    private Runnable postSaveAction;

    private GenericEditPopup() {
        this.fieldConfigs = new ArrayList<>();
    }

    public static <T> GenericEditPopup<T> create(Class<T> clazz) {
        return new GenericEditPopup<>();
    }

    public GenericEditPopup<T> withTitle(String title) {
        this.title = title;
        return this;
    }

    public GenericEditPopup<T> forItem(T item) {
        this.item = item;
        return this;
    }

    public GenericEditPopup<T> addField(String label, Control field, boolean isDisabled) {
        field.setDisable(isDisabled);
        this.fieldConfigs.add(new FieldConfig<>(label, field));
        return this;
    }

    public GenericEditPopup<T> addField(String label, Control field) {
        return addField(label, field, false);
    }

    public GenericEditPopup<T> onSave(BiConsumer<T, List<Control>> saveAction) {
        this.saveAction = saveAction;
        return this;
    }

    public GenericEditPopup<T> afterSave(Runnable postSaveAction) {
        this.postSaveAction = postSaveAction;
        return this;
    }

    public void show() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);

        ButtonType confirmButtonType = new ButtonType("Konfirmasi", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        List<Control> fields = new ArrayList<>();
        for (int i = 0; i < fieldConfigs.size(); i++) {
            FieldConfig<T> config = fieldConfigs.get(i);
            grid.add(new Label(config.getLabel() + ":"), 0, i);
            grid.add(config.getField(), 1, i);
            fields.add(config.getField());
        }

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                if (ShowAlert.showYesNo("Konfirmasi Perubahan", 
                    "Apakah Anda yakin ingin menyimpan perubahan ini?")) {
                    try {
                        if (saveAction != null) {
                            saveAction.accept(item, fields);
                        }
                        if (postSaveAction != null) {
                            postSaveAction.run();
                        }
                        ShowAlert.showSuccess("Data berhasil disimpan");
                    } catch (Exception e) {
                        ShowAlert.showError("Gagal menyimpan data: " + e.getMessage());
                    }
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private static class FieldConfig<T> {
        private final String label;
        private final Control field;

        public FieldConfig(String label, Control field) {
            this.label = label;
            this.field = field;
        }

        public String getLabel() {
            return label;
        }

        public Control getField() {
            return field;
        }
    }
}