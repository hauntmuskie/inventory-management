package com.lestarieragemilang.desktop.controller;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
        setDefaultComboBoxValues(); // Add this line
        initializeCategoryTable();
        loadCategories();
        generateAndSetCategoryId();

        categoryIdField.setDisable(true);

        initializeSearch();
    }

    private void setDefaultComboBoxValues() {
        brandComboBox.setValue("Nike");
        typeComboBox.setValue("Shoes");
        sizeComboBox.setValue("M");
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
        brandComboBox.setItems(FXCollections.observableArrayList("Nike", "Adidas", "Puma", "Reebok", "Under Armour"));
        typeComboBox.setItems(FXCollections.observableArrayList("Shoes", "T-shirt", "Shorts", "Jacket", "Socks"));
        sizeComboBox.setItems(FXCollections.observableArrayList("XS", "S", "M", "L", "XL", "XXL"));
        weightUnitComboBox.setItems(FXCollections.observableArrayList("kg", "g", "lb", "oz"));
    }

    private void initializeCategoryTable() {
        List<TableColumn<Category, ?>> columns = List.of(
                TableUtils.createColumn("Category ID", "categoryId"),
                TableUtils.createColumn("Brand", "brand"),
                TableUtils.createColumn("Type", "productType"),
                TableUtils.createColumn("Size", "size"),
                TableUtils.createColumn("Weight", "weight"),
                TableUtils.createColumn("Unit", "weightUnit"));
        TableUtils.populateTable(categoryTable, columns, categoryService.findAll());
    }

    private void loadCategories() {
        List<Category> categories = categoryService.findAll();
        categoryTable.setItems(FXCollections.observableArrayList(categories));
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
        String categoryId = categoryIdField.getText();
        if (categoryIdExists(categoryId)) {
            ShowAlert.showWarning("ID Kategori sudah ada di database");
            generateAndSetCategoryId();
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
    }

    @FXML
    private void handleUpdate() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            ShowAlert.showWarning("Silahkan pilih kategori yang akan diubah");
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
    }

    @FXML
    private void handleDelete() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            ShowAlert.showWarning("Silahkan pilih kategori yang akan dihapus");
            return;
        }

        if (ShowAlert.showYesNo("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus data kategori ini?")) {
            categoryService.delete(selectedCategory);
            ShowAlert.showSuccess("Data kategori berhasil dihapus");
            loadCategories();
            clearFields();
        }
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

        // Set default values based on the selected category
        brandComboBox.setValue(selectedCategory.getBrand());
        typeComboBox.setValue(selectedCategory.getProductType());
        sizeComboBox.setValue(selectedCategory.getSize());
        weightUnitComboBox.setValue(selectedCategory.getWeightUnit());

        GenericEditPopup.create(Category.class)
                .withTitle("Edit Category")
                .forItem(selectedCategory)
                .addField("Category ID", new TextField(selectedCategory.getCategoryId()), true)
                .addField("Brand", brandComboBox) // Use the pre-set brandComboBox
                .addField("Type", typeComboBox) // Use the pre-set typeComboBox
                .addField("Size", sizeComboBox) // Use the pre-set sizeComboBox
                .addField("Weight", new TextField(selectedCategory.getWeight().toString()))
                .addField("Weight Unit", weightUnitComboBox) // Use the pre-set weightUnitComboBox
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