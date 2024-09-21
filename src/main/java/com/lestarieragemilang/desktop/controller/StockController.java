package com.lestarieragemilang.desktop.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.util.StringConverter;
import org.hibernate.exception.ConstraintViolationException;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.lestarieragemilang.desktop.model.Category;
import com.lestarieragemilang.desktop.model.Stock;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.service.GenericService;
import com.lestarieragemilang.desktop.utils.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class StockController extends HibernateUtil {
    @FXML
    private TextField stockIDIncrement, stockQuantityField, stockBuyPriceField, stockSellPriceField, stockSearchField;
    @FXML
    private JFXComboBox<Category> categoryIDDropDown;
    @FXML
    private TableView<Stock> stockTable;
    @FXML
    private JFXButton editStockButtonText;

    private final GenericService<Stock> stockService;
    private final GenericService<Category> categoryService;

    public StockController() {
        this.stockService = new GenericService<>(new GenericDao<>(Stock.class), "STK", 3);
        this.categoryService = new GenericService<>(new GenericDao<>(Category.class), "CAT", 3);
    }

    public void initialize() {
        initializeCategoryComboBox();
        initializeStockTable();
        CompletableFuture.runAsync(this::loadStocks);
        generateAndSetStockId();
        stockIDIncrement.setDisable(true);
        stockSearchField.textProperty().addListener((observable, oldValue, newValue) -> handleSearch());
    }

    private void initializeCategoryComboBox() {
        List<Category> categories = categoryService.findAll();
        categoryIDDropDown.setItems(FXCollections.observableArrayList(categories));
        categoryIDDropDown.setValue(categories.get(0));
        categoryIDDropDown.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return Optional.ofNullable(category)
                        .map(c -> c.getCategoryId() + " - " + c.getBrand() + " " + c.getProductType())
                        .orElse("");
            }

            @Override
            public Category fromString(String string) {
                return null;
            }
        });
    }

    private void initializeStockTable() {
        List<TableColumn<Stock, ?>> columns = List.of(
                TableUtils.createColumn("Stock ID", "stockId"),
                TableUtils.createColumn("Category ID", "category.categoryId"),
                TableUtils.createColumn("Brand", "category.brand"),
                TableUtils.createColumn("Type", "category.productType"),
                TableUtils.createColumn("Size", "category.size"),
                TableUtils.createColumn("Weight", "category.weight"),
                TableUtils.createColumn("Unit", "category.weightUnit"),
                TableUtils.createColumn("Quantity", "quantity"),
                TableUtils.createFormattedColumn("Buy Price", "purchasePrice"),
                TableUtils.createFormattedColumn("Sell Price", "sellingPrice"));
        TableUtils.populateTable(stockTable, columns, stockService.findAll());
    }

    private void loadStocks() {
        List<Stock> stocks = stockService.findAll();
        javafx.application.Platform.runLater(() -> stockTable.setItems(FXCollections.observableArrayList(stocks)));
    }

    private void generateAndSetStockId() {
        String newStockId;
        do {
            newStockId = IdGenerator.generateRandomId("STK", 1000);
        } while (stockIdExists(newStockId));

        stockIDIncrement.setText(newStockId);
    }

    private boolean stockIdExists(String stockId) {
        return stockService.findAll().stream()
                .anyMatch(stock -> stock.getStockId().equals(stockId));
    }

    @FXML
    private void addStockButton() {
        String stockId = stockIDIncrement.getText();
        if (stockIdExists(stockId)) {
            ShowAlert.showAlert(AlertType.WARNING, "Stock ID Exists",
                    "Stock ID already exists. Please generate a new one.");
            generateAndSetStockId();
            return;
        }

        Stock stock = new Stock();
        stock.setStockId(stockId);
        stock.setCategory(categoryIDDropDown.getValue());
        stock.setQuantity(Integer.parseInt(stockQuantityField.getText()));
        stock.setPurchasePrice(new BigDecimal(stockBuyPriceField.getText()));
        stock.setSellingPrice(new BigDecimal(stockSellPriceField.getText()));

        CompletableFuture.runAsync(() -> stockService.save(stock))
                .thenRun(this::loadStocks)
                .thenRun(this::resetStockButton);
    }

    @FXML
    private void resetStockButton() {
        ClearFields.clearFields(stockIDIncrement, categoryIDDropDown, stockQuantityField, stockBuyPriceField,
                stockSellPriceField);
        generateAndSetStockId();
    }

    @SuppressWarnings("unchecked")
    @FXML
    private void editStockButton() {
        Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            ShowAlert.showAlert(AlertType.WARNING, "No Stock Selected", null, "Please select a stock to edit.");
            return;
        }

        JFXComboBox<Category> categoryComboBox = new JFXComboBox<>(categoryIDDropDown.getItems());

        // Find the category in the list that matches the selectedStock's category by ID
        for (Category category : categoryIDDropDown.getItems()) {
            if (category.getId().equals(selectedStock.getCategory().getId())) {
                categoryComboBox.setValue(category); // Set the matching category
                break;
            }
        }

        GenericEditPopup.create(Stock.class)
                .withTitle("Edit Stock")
                .forItem(selectedStock)
                .addField("Stock ID", new TextField(selectedStock.getStockId()), true)
                .addField("Category", categoryComboBox) // Use the pre-set categoryComboBox
                .addField("Quantity", new TextField(String.valueOf(selectedStock.getQuantity())))
                .addField("Purchase Price", new TextField(selectedStock.getPurchasePrice().toString()))
                .addField("Selling Price", new TextField(selectedStock.getSellingPrice().toString()))
                .onSave((stock, fields) -> {
                    stock.setCategory(((JFXComboBox<Category>) fields.get(1)).getValue());
                    stock.setQuantity(Integer.parseInt(((TextField) fields.get(2)).getText()));
                    stock.setPurchasePrice(new BigDecimal(((TextField) fields.get(3)).getText()));
                    stock.setSellingPrice(new BigDecimal(((TextField) fields.get(4)).getText()));
                    CompletableFuture.runAsync(() -> stockService.update(stock));
                })
                .afterSave(() -> {
                    loadStocks();
                    resetStockButton();
                })
                .show();
    }

    @FXML
    private void removeStockButton() {
        Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            ShowAlert.showAlert(AlertType.INFORMATION, "No Stock Selected", null, "Please select a stock to remove.");
            return;
        }

        if (!stockService.canDelete(selectedStock)) {
            ShowAlert.showAlert(AlertType.ERROR, "Cannot Delete Stock", null,
                    "This stock cannot be deleted because it is referenced by other records.");
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this stock?",
                ButtonType.YES, ButtonType.NO);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    CompletableFuture.runAsync(() -> stockService.delete(selectedStock))
                            .thenRun(this::loadStocks)
                            .thenRun(this::resetStockButton);
                } catch (ConstraintViolationException e) {
                    ShowAlert.showAlert(AlertType.ERROR, "Error Deleting Stock", null,
                            "An unexpected error occurred while trying to delete the stock. It may be referenced by other records.");
                }
            }
        });
    }

    @FXML
    private void handleSearch() {
        String searchTerm = nullToEmpty(stockSearchField.getText()).toLowerCase();
        List<Stock> allStocks = stockService.findAll();
        Predicate<Stock> searchPredicate = stock -> stock.getStockId().toLowerCase().contains(searchTerm) ||
                stock.getCategory().getCategoryId().toLowerCase().contains(searchTerm) ||
                stock.getCategory().getBrand().toLowerCase().contains(searchTerm) ||
                stock.getCategory().getProductType().toLowerCase().contains(searchTerm);

        List<Stock> filteredStocks = allStocks.stream()
                .filter(searchPredicate)
                .toList();
        stockTable.setItems(FXCollections.observableArrayList(filteredStocks));
    }

    private String nullToEmpty(String str) {
        return str == null ? "" : str;
    }
}