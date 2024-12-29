package com.lestarieragemilang.desktop.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.util.Optional;
import java.util.function.BiConsumer;
import javafx.scene.control.TextInputControl;
import javafx.application.Platform;

public final class GenericEditPopup<T> {
    private static final String ERROR_CLASS_NULL = "Tipe kelas tidak boleh kosong";
    private static final String ERROR_TITLE_NULL = "Judul tidak boleh kosong";
    private static final String ERROR_ITEM_NULL = "Data tidak boleh kosong";
    private static final String ERROR_LABEL_NULL = "Label tidak boleh kosong";
    private static final String ERROR_FIELD_NULL = "Field tidak boleh kosong";
    private static final String ERROR_SAVE_ACTION_NULL = "Aksi simpan tidak boleh kosong";
    private static final String ERROR_TITLE_REQUIRED = "Judul harus diatur sebelum menampilkan dialog";
    private static final String ERROR_ITEM_REQUIRED = "Item harus diatur sebelum menampilkan dialog";
    
    private final ImmutableList.Builder<FieldConfig<T>> fieldConfigsBuilder;
    private Optional<String> title;
    private Optional<T> item;
    private Optional<BiConsumer<T, ImmutableList<Control>>> saveAction;
    private Optional<Runnable> postSaveAction;

    private GenericEditPopup() {
        this.fieldConfigsBuilder = ImmutableList.builder();
        this.title = Optional.empty();
        this.item = Optional.empty();
        this.saveAction = Optional.empty();
        this.postSaveAction = Optional.empty();
    }

    public static <T> GenericEditPopup<T> create(Class<T> clazz) {
        Preconditions.checkNotNull(clazz, ERROR_CLASS_NULL);
        return new GenericEditPopup<>();
    }

    public GenericEditPopup<T> withTitle(String title) {
        this.title = Optional.of(Preconditions.checkNotNull(title, ERROR_TITLE_NULL));
        return this;
    }

    public GenericEditPopup<T> forItem(T item) {
        this.item = Optional.of(Preconditions.checkNotNull(item, ERROR_ITEM_NULL));
        return this;
    }

    public GenericEditPopup<T> addField(String label, Control field, boolean isDisabled) {
        Preconditions.checkNotNull(label, ERROR_LABEL_NULL);
        Preconditions.checkNotNull(field, ERROR_FIELD_NULL);

        if (field instanceof TextInputControl) {
            initializeTextField((TextInputControl) field);
        }

        field.setDisable(isDisabled);
        fieldConfigsBuilder.add(new FieldConfig<>(label, field));
        return this;
    }

    public GenericEditPopup<T> addField(String label, Control field) {
        return addField(label, field, false);
    }

    public GenericEditPopup<T> onSave(BiConsumer<T, ImmutableList<Control>> saveAction) {
        this.saveAction = Optional.of(Preconditions.checkNotNull(saveAction, ERROR_SAVE_ACTION_NULL));
        return this;
    }

    public GenericEditPopup<T> afterSave(Runnable postSaveAction) {
        this.postSaveAction = Optional.of(Preconditions.checkNotNull(postSaveAction, "Post save action cannot be null"));
        return this;
    }

    private void initializeTextField(TextInputControl textField) {
        Platform.runLater(() -> {
            try {
                textField.setText(Optional.ofNullable(textField.getText()).orElse(""));
            } catch (Exception e) {
                textField.setText("");
            }
        });

        textField.textProperty().addListener((_, oldValue, newValue) -> {
            if (newValue == null) {
                Platform.runLater(() -> textField.setText(Optional.ofNullable(oldValue).orElse("")));
            }
        });
    }

    private void validateTextFields(ImmutableList<Control> fields) {
        fields.stream()
            .filter(field -> field instanceof TextInputControl)
            .map(field -> (TextInputControl) field)
            .forEach(this::initializeTextField);
    }

    public void show() {
        Verify.verify(title.isPresent(), ERROR_TITLE_REQUIRED);
        Verify.verify(item.isPresent(), ERROR_ITEM_REQUIRED);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title.get());

        ButtonType confirmButtonType = new ButtonType("Konfirmasi", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = createGridPane();
        ImmutableList<Control> fields = setupFields(grid);
        dialog.getDialogPane().setContent(grid);

        setupDialogActions(dialog, confirmButtonType, fields);
        dialog.showAndWait();
    }

    private GridPane createGridPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        return grid;
    }

    private ImmutableList<Control> setupFields(GridPane grid) {
        ImmutableList<FieldConfig<T>> fieldConfigs = fieldConfigsBuilder.build();
        ImmutableList.Builder<Control> fieldsBuilder = ImmutableList.builder();

        for (int i = 0; i < fieldConfigs.size(); i++) {
            FieldConfig<T> config = fieldConfigs.get(i);
            grid.add(new Label(config.getLabel() + ":"), 0, i);

            Control field = config.getField();
            if (field instanceof TextInputControl) {
                validateTextFields(ImmutableList.of(field));
            }

            grid.add(field, 1, i);
            fieldsBuilder.add(field);
        }

        return fieldsBuilder.build();
    }

    private void setupDialogActions(Dialog<ButtonType> dialog, ButtonType confirmButtonType, ImmutableList<Control> fields) {
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType && ShowAlert.showYesNo("Konfirmasi Perubahan",
                    "Apakah Anda yakin ingin menyimpan perubahan ini?")) {
                handleSave(fields);
            }
            return null;
        });
    }

    private void handleSave(ImmutableList<Control> fields) {
        try {
            Platform.runLater(() -> validateTextFields(fields));
            Thread.sleep(100);

            saveAction.ifPresent(action -> action.accept(item.get(), fields));
            postSaveAction.ifPresent(Runnable::run);
            ShowAlert.showSuccess("Data berhasil disimpan");
        } catch (Exception e) {
            ShowAlert.showError("Gagal menyimpan data: " + e.getMessage());
        }
    }

    private static final class FieldConfig<T> {
        private final String label;
        private final Control field;

        public FieldConfig(String label, Control field) {
            this.label = Preconditions.checkNotNull(label, ERROR_LABEL_NULL);
            this.field = Preconditions.checkNotNull(field, ERROR_FIELD_NULL);
        }

        public String getLabel() {
            return label;
        }

        public Control getField() {
            return field;
        }
    }
}