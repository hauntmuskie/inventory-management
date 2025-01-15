package com.lestarieragemilang.desktop.controller;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import com.google.common.base.Strings;
import com.lestarieragemilang.desktop.model.Category;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.service.GenericService;
import com.lestarieragemilang.desktop.utils.ClearFields;
import com.lestarieragemilang.desktop.utils.GenericEditPopup;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.TableUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

public class CategoryController extends HibernateUtil {
    @FXML
    private TextField categoryIdField, weightField, searchField;
    @FXML
    private ComboBox<String> brandComboBox, typeComboBox, sizeComboBox, weightUnitComboBox;
    @FXML
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, String> categoryIdColumn, brandColumn, productTypeColumn, sizeColumn,
            weightUnitColumn;
    @FXML
    private TableColumn<Category, BigDecimal> weightColumn;

    private GenericService<Category> categoryService;
    private FilteredList<Category> filteredCategories;

    public void initialize() {
        categoryService = new GenericService<>(new GenericDao<>(Category.class), "CAT", 3);

        initializeComboBoxes();
        setDefaultComboBoxValues();
        initializeCategoryTable();
        loadCategories();
        generateAndSetCategoryId();

        categoryIdField.setDisable(true);

        initializeSearch();
    }

    private void setDefaultComboBoxValues() {
        brandComboBox.setValue("Krakatau Steel");
        typeComboBox.setValue("Pipe");
        sizeComboBox.setValue("6 inch");
        weightUnitComboBox.setValue("kg");
    }

    private void initializeSearch() {
        filteredCategories = new FilteredList<>(categoryTable.getItems(), _ -> true);
        searchField.textProperty().addListener((_, _, newValue) -> {
            filteredCategories.setPredicate(category -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();

                if (category.getCategoryId().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (category.getBrand().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (category.getProductType().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (category.getSize().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (String.valueOf(category.getWeight()).toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else
                    return category.getWeightUnit().toLowerCase().contains(lowerCaseFilter);
            });
        });

        categoryTable.setItems(filteredCategories);
    }

    private void initializeComboBoxes() {
        brandComboBox.setItems(FXCollections.observableArrayList(
            "Krakatau Steel",
            "Gunung Steel",
            "Master Steel",
            "Ispat Indo",
            "Gunawan Dianjaya Steel",
            "Steel Pipe Industry",
            "Jayapari Steel",
            "Jaya Steel",
            "Indonusa Steel",
            "Sarana Central Bajatama",
            "Cilegon Steel",
            "Hanil Jaya Steel"
        ));
        typeComboBox.setItems(FXCollections.observableArrayList(
            "Sheet", 
            "Rod", 
            "Beam", 
            "Tube", 
            "Plate", 
            "Coil",
            "Pipe",
            "Wire",
            "Angle",
            "Channel"
        ));
        sizeComboBox.setItems(FXCollections.observableArrayList(
            "4x8 ft",
            "5x10 ft",
            "1 inch",
            "2 inch",
            "3 inch",
            "4 inch",
            "6 inch",
            "8 inch",
            "10 inch",
            "12 inch"
        ));
        weightUnitComboBox.setItems(FXCollections.observableArrayList("kg", "ton", "g"));
    }

    private void initializeCategoryTable() {
        List<TableColumn<Category, ?>> columns = List.of(
                TableUtils.createColumn("Kode Kategori", "categoryId"),
                TableUtils.createColumn("Merek", "brand"),
                TableUtils.createColumn("Tipe", "productType"),
                TableUtils.createColumn("Ukuran", "size"),
                TableUtils.createColumn("Berat", "weight"),
                TableUtils.createColumn("Satuan", "weightUnit"));
        TableUtils.populateTable(categoryTable, columns, categoryService.findAll());
    }

    private void loadCategories() {
        try {
            List<Category> categories = categoryService.findAll();
            categoryTable.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            ShowAlert.showDatabaseError("Gagal memuat data kategori: " + e.getMessage());
        }
    }

    private void generateAndSetCategoryId() {
        String newCategoryId;
        do {
            newCategoryId = generateRandomCategoryId();
        } while (categoryIdExists(newCategoryId));

        categoryIdField.setText(newCategoryId);
    }

    private String generateRandomCategoryId() {
        Random random = new Random();
        int randomNumber = random.nextInt(1000);
        return String.format("CAT-%03d", randomNumber);
    }

    private boolean categoryIdExists(String categoryId) {
        return categoryService.findAll().stream()
                .anyMatch(category -> category.getCategoryId().equals(categoryId));
    }

    @FXML
    private void handleSave() {
        try {
            if (!validateFields()) {
                return;
            }

            String categoryId = categoryIdField.getText();
            if (categoryIdExists(categoryId)) {
                ShowAlert.showWarning("Kode Kategori sudah ada di database");
                generateAndSetCategoryId();
                return;
            }

            if (!ShowAlert.showConfirmation("Konfirmasi Simpan", "Konfirmasi Simpan Data", 
                "Apakah Anda yakin ingin menyimpan data kategori ini?")) {
                return;
            }

            Category category = new Category();
            category.setCategoryId(categoryId);
            category.setBrand(brandComboBox.getValue());
            category.setProductType(typeComboBox.getValue());
            category.setSize(sizeComboBox.getValue());
            category.setWeight(new BigDecimal(weightField.getText()));
            category.setWeightUnit(weightUnitComboBox.getValue());

            categoryService.save(category);
            ShowAlert.showSuccess("Data kategori berhasil ditambahkan");
            loadCategories();
            clearFields();
        } catch (NumberFormatException e) {
            ShowAlert.showError("Nilai berat harus berupa angka");
        } catch (Exception e) {
            ShowAlert.showError("Terjadi kesalahan: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        try {
            Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
            if (selectedCategory == null) {
                ShowAlert.showWarning("Silahkan pilih kategori yang akan diubah");
                return;
            }

            if (!validateFields()) {
                return;
            }

            if (!ShowAlert.showConfirmation("Konfirmasi Ubah", "Konfirmasi Ubah Data", 
                "Apakah Anda yakin ingin mengubah data kategori ini?")) {
                return;
            }

            Category category = new Category();
            category.setId(selectedCategory.getId());
            category.setCategoryId(selectedCategory.getCategoryId());
            category.setBrand(brandComboBox.getValue());
            category.setProductType(typeComboBox.getValue());
            category.setSize(sizeComboBox.getValue());
            category.setWeight(new BigDecimal(weightField.getText()));
            category.setWeightUnit(weightUnitComboBox.getValue());

            categoryService.update(category);
            ShowAlert.showSuccess("Data kategori berhasil diubah");
            loadCategories();
            clearFields();
        } catch (NumberFormatException e) {
            ShowAlert.showError("Nilai berat harus berupa angka");
        } catch (Exception e) {
            ShowAlert.showError("Terjadi kesalahan: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        try {
            Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
            if (selectedCategory == null) {
                ShowAlert.showWarning("Silahkan pilih kategori yang akan dihapus");
                return;
            }

            if (!ShowAlert.showConfirmation("Konfirmasi Hapus", "Konfirmasi Hapus Data", 
                "Apakah Anda yakin ingin menghapus data kategori ini?")) {
                return;
            }

            categoryService.delete(selectedCategory);
            ShowAlert.showSuccess("Data kategori berhasil dihapus");
            loadCategories();
            clearFields();
        } catch (Exception e) {
            ShowAlert.showError("Terjadi kesalahan saat menghapus: " + e.getMessage());
        }
    }

    private boolean validateFields() {
        if (Strings.isNullOrEmpty(categoryIdField.getText())) {
            ShowAlert.showValidationError("Kode kategori tidak boleh kosong");
            return false;
        }
        if (brandComboBox.getValue() == null) {
            ShowAlert.showValidationError("Merek harus dipilih");
            return false;
        }
        if (typeComboBox.getValue() == null) {
            ShowAlert.showValidationError("Tipe produk harus dipilih");
            return false;
        }
        if (sizeComboBox.getValue() == null) {
            ShowAlert.showValidationError("Ukuran harus dipilih");
            return false;
        }
        if (Strings.isNullOrEmpty(weightField.getText())) {
            ShowAlert.showValidationError("Berat tidak boleh kosong");
            return false;
        }
        if (weightUnitComboBox.getValue() == null) {
            ShowAlert.showValidationError("Satuan berat harus dipilih");
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    @FXML
    private void handleEdit() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            ShowAlert.showWarning("Silahkan pilih kategori yang akan diubah");
            return;
        }

        ComboBox<String> brandComboBox = new ComboBox<>(this.brandComboBox.getItems());
        ComboBox<String> typeComboBox = new ComboBox<>(this.typeComboBox.getItems());
        ComboBox<String> sizeComboBox = new ComboBox<>(this.sizeComboBox.getItems());
        ComboBox<String> weightUnitComboBox = new ComboBox<>(this.weightUnitComboBox.getItems());

        brandComboBox.setValue(selectedCategory.getBrand());
        typeComboBox.setValue(selectedCategory.getProductType());
        sizeComboBox.setValue(selectedCategory.getSize());
        weightUnitComboBox.setValue(selectedCategory.getWeightUnit());

        GenericEditPopup.create(Category.class)
                .withTitle("Ubah Kategori")
                .forItem(selectedCategory)
                .addField("Kode Kategori", new TextField(selectedCategory.getCategoryId()), true)
                .addField("Merek", brandComboBox)
                .addField("Tipe", typeComboBox)
                .addField("Ukuran", sizeComboBox)
                .addField("Berat", new TextField(selectedCategory.getWeight().toString()))
                .addField("Satuan Berat", weightUnitComboBox)
                .onSave((category, fields) -> {
                    category.setBrand(((ComboBox<String>) fields.get(1)).getValue());
                    category.setProductType(((ComboBox<String>) fields.get(2)).getValue());
                    category.setSize(((ComboBox<String>) fields.get(3)).getValue());
                    category.setWeight(new BigDecimal(((TextField) fields.get(4)).getText()));
                    category.setWeightUnit(((ComboBox<String>) fields.get(5)).getValue());
                    categoryService.update(category);
                })
                .afterSave(() -> {
                    ShowAlert.showSuccess("Data kategori berhasil diubah");
                    loadCategories();
                    clearFields();
                    categoryTable.refresh();
                })
                .show();
    }

    @FXML
    private void handleClear() {
        ClearFields.clearFields(categoryIdField, brandComboBox, typeComboBox, sizeComboBox, weightField,
                weightUnitComboBox);
        generateAndSetCategoryId();
    }

    private void clearFields() {
        handleClear();
    }
}