package com.lestarieragemilang.desktop.controller;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import com.lestarieragemilang.desktop.App;
import com.lestarieragemilang.desktop.model.Category;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.service.GenericService;
import com.lestarieragemilang.desktop.utils.ClearFields;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.TableUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

public class CategoryController {
    @FXML
    private TextField categoryIdField;
    @FXML
    private ComboBox<String> brandComboBox;
    @FXML
    private ComboBox<String> typeComboBox;
    @FXML
    private ComboBox<String> sizeComboBox;
    @FXML
    private TextField weightField;
    @FXML
    private ComboBox<String> weightUnitComboBox;
    @FXML
    private TableView<Category> categoryTable;
    @FXML
    private TableColumn<Category, String> categoryIdColumn;
    @FXML
    private TableColumn<Category, String> brandColumn;
    @FXML
    private TableColumn<Category, String> productTypeColumn;
    @FXML
    private TableColumn<Category, String> sizeColumn;
    @FXML
    private TableColumn<Category, BigDecimal> weightColumn;
    @FXML
    private TableColumn<Category, String> weightUnitColumn;
    @FXML
    private TextField searchField;

    private GenericService<Category> categoryService;
    private FilteredList<Category> filteredCategories;

    public void initialize() {
        SessionFactory sessionFactory = new Configuration().configure(
                App.class.getResource("hibernate.cfg.xml")).buildSessionFactory();
        categoryService = new GenericService<>(new GenericDao<>(Category.class, sessionFactory), "CAT");

        initializeComboBoxes();
        initializeCategoryTable();
        loadCategories();
        generateAndSetCategoryId();

        categoryIdField.setDisable(true);

        initializeSearch();
    }

    private void initializeSearch() {
        filteredCategories = new FilteredList<>(categoryTable.getItems(), p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
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
        typeComboBox
                .setItems(FXCollections.observableArrayList("Shoes", "T-shirt", "Shorts", "Jacket", "Socks"));
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
            ShowAlert.showAlert("Category ID already exists. Please try again.");
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
        loadCategories();
        clearFields();
    }

    @FXML
    private void handleUpdate() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            ShowAlert.showAlert("Please select a category to update.");
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
        loadCategories();
        clearFields();
    }

    @FXML
    private void handleDelete() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            ShowAlert.showAlert("Please select a category to delete.");
            return;
        }

        categoryService.delete(selectedCategory);
        loadCategories();
        clearFields();
    }

    @FXML
    private void handleEdit() {
        Category selectedCategory = categoryTable.getSelectionModel().getSelectedItem();
        if (selectedCategory == null) {
            ShowAlert.showAlert("Please select a category to edit.");
            return;
        }

        categoryIdField.setText(selectedCategory.getCategoryId());
        brandComboBox.setValue(selectedCategory.getBrand());
        typeComboBox.setValue(selectedCategory.getProductType());
        sizeComboBox.setValue(selectedCategory.getSize());
        weightField.setText(selectedCategory.getWeight().toString());
        weightUnitComboBox.setValue(selectedCategory.getWeightUnit());
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