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
import javafx.scene.Node;

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

    /**
     * Creates a new instance of GenericEditPopup for the specified class type.
     *
     * @param <T> the type of item being edited
     * @param clazz the class of the item being edited
     * @return a new GenericEditPopup instance
     * @throws NullPointerException if clazz is null
     */
    public static <T> GenericEditPopup<T> create(Class<T> clazz) {
        Preconditions.checkNotNull(clazz, ERROR_CLASS_NULL);
        return new GenericEditPopup<>();
    }

    /**
     * Sets the title for the edit dialog.
     *
     * @param title the dialog title
     * @return this GenericEditPopup instance for method chaining
     * @throws NullPointerException if title is null
     */
    public GenericEditPopup<T> withTitle(String title) {
        this.title = Optional.of(Preconditions.checkNotNull(title, ERROR_TITLE_NULL));
        return this;
    }

    /**
     * Sets the item to be edited in the dialog.
     *
     * @param item the item to edit
     * @return this GenericEditPopup instance for method chaining
     * @throws NullPointerException if item is null
     */
    public GenericEditPopup<T> forItem(T item) {
        this.item = Optional.of(Preconditions.checkNotNull(item, ERROR_ITEM_NULL));
        return this;
    }

    /**
     * Adds a field to the edit dialog with a label and optional disabled state.
     *
     * @param label the label for the field
     * @param field the input field node
     * @param isDisabled whether the field should be disabled
     * @return this GenericEditPopup instance for method chaining
     * @throws NullPointerException if label or field is null
     */
    public GenericEditPopup<T> addField(String label, Node field, boolean isDisabled) {
        Preconditions.checkNotNull(label, ERROR_LABEL_NULL);
        Preconditions.checkNotNull(field, ERROR_FIELD_NULL);

        if (field instanceof TextInputControl) {
            initializeTextField((TextInputControl) field);
        }

        if (field instanceof Control) {
            ((Control) field).setDisable(isDisabled);
        }
        
        fieldConfigsBuilder.add(new FieldConfig<>(label, field));
        return this;
    }

    /**
     * Adds an enabled field to the edit dialog with a label.
     *
     * @param label the label for the field
     * @param field the input field node
     * @return this GenericEditPopup instance for method chaining
     * @throws NullPointerException if label or field is null
     */
    public GenericEditPopup<T> addField(String label, Node field) {
        return addField(label, field, false);
    }

    /**
     * Sets the action to be performed when saving the edited item.
     *
     * @param saveAction the action to perform, accepting the edited item and list of controls
     * @return this GenericEditPopup instance for method chaining
     * @throws NullPointerException if saveAction is null
     */
    public GenericEditPopup<T> onSave(BiConsumer<T, ImmutableList<Control>> saveAction) {
        this.saveAction = Optional.of(Preconditions.checkNotNull(saveAction, ERROR_SAVE_ACTION_NULL));
        return this;
    }

    /**
     * Sets the action to be performed after successful save completion.
     *
     * @param postSaveAction the action to perform after saving
     * @return this GenericEditPopup instance for method chaining
     * @throws NullPointerException if postSaveAction is null
     */
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

    /**
     * Shows the edit dialog and handles user interaction.
     * Must be called after setting title and item.
     *
     * @throws IllegalStateException if title or item is not set
     */
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

            Node field = config.getField();
            if (field instanceof Control) {
                if (field instanceof TextInputControl) {
                    validateTextFields(ImmutableList.of((Control) field));
                }
                fieldsBuilder.add((Control) field);
            }
            
            grid.add(field, 1, i);
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
        private final Node field;

        public FieldConfig(String label, Node field) {
            this.label = Preconditions.checkNotNull(label, ERROR_LABEL_NULL);
            this.field = Preconditions.checkNotNull(field, ERROR_FIELD_NULL);
        }

        public String getLabel() {
            return label;
        }

        public Node getField() {
            return field;
        }
    }
}