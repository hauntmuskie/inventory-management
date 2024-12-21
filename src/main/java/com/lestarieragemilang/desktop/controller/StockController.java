package com.lestarieragemilang.desktop.controller;

import javafx.application.Platform;
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
        initializeNumberFormatting();
        CompletableFuture.runAsync(this::loadStocks);
        generateAndSetStockId();
        stockIDIncrement.setDisable(true);
        stockSearchField.textProperty().addListener((_, _, _) -> handleSearch());
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

    private void initializeNumberFormatting() {
        NumberFormatter.applyNumberFormat(stockBuyPriceField);
        NumberFormatter.applyNumberFormat(stockSellPriceField);
    }

    private void loadStocks() {
        CompletableFuture.runAsync(() -> {
            List<Stock> stocks = stockService.findAll();
            Platform.runLater(() -> stockTable.setItems(FXCollections.observableArrayList(stocks)));
        });
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
            ShowAlert.showWarning("ID Stok sudah ada di database.");
            generateAndSetStockId();
            return;
        }

        Stock stock = new Stock();
        stock.setStockId(stockId);
        stock.setCategory(categoryIDDropDown.getValue());
        stock.setQuantity(Integer.parseInt(stockQuantityField.getText()));
        stock.setPurchasePrice(new BigDecimal(NumberFormatter.getNumericValue(stockBuyPriceField.getText())));
        stock.setSellingPrice(new BigDecimal(NumberFormatter.getNumericValue(stockSellPriceField.getText())));

        CompletableFuture.runAsync(() -> stockService.save(stock))
                .thenRun(() -> Platform.runLater(() -> {
                    loadStocks();
                    resetStockButton();
                }));
    }

    @FXML
    private void resetStockButton() {
        Platform.runLater(() -> {
            ClearFields.clearFields(stockIDIncrement, categoryIDDropDown, stockQuantityField, stockBuyPriceField,
                    stockSellPriceField);
            generateAndSetStockId();
        });
    }

    @SuppressWarnings("unchecked")
    @FXML
    private void editStockButton() {
        Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            ShowAlert.showWarning("Silakan pilih stok yang akan diubah");
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
                .addField("Purchase Price", createFormattedTextField(selectedStock.getPurchasePrice()))
                .addField("Selling Price", createFormattedTextField(selectedStock.getSellingPrice()))
                .onSave((stock, fields) -> {
                    stock.setCategory(((JFXComboBox<Category>) fields.get(1)).getValue());
                    stock.setQuantity(Integer.parseInt(((TextField) fields.get(2)).getText()));
                    stock.setPurchasePrice(
                            new BigDecimal(NumberFormatter.getNumericValue(((TextField) fields.get(3)).getText())));
                    stock.setSellingPrice(
                            new BigDecimal(NumberFormatter.getNumericValue(((TextField) fields.get(4)).getText())));
                    CompletableFuture.runAsync(() -> stockService.update(stock));
                })
                .afterSave(() -> {
                    loadStocks();
                    resetStockButton();
                })
                .show();
    }

    private TextField createFormattedTextField(BigDecimal value) {
        TextField field = new TextField(NumberFormatter.formatValue(value));
        NumberFormatter.applyNumberFormat(field);
        return field;
    }

    @FXML
    private void removeStockButton() {
        Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            ShowAlert.showWarning("Silakan pilih stok yang akan dihapus");
            return;
        }

        if (!stockService.canDelete(selectedStock)) {
            ShowAlert.showError("Stok tidak dapat dihapus karena masih terhubung dengan data lain");
            return;
        }

        if (ShowAlert.showYesNo("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus stok ini?")) {
            try {
                CompletableFuture.runAsync(() -> stockService.delete(selectedStock))
                        .thenRun(() -> Platform.runLater(() -> {
                            ShowAlert.showSuccess("Data stok berhasil dihapus");
                            loadStocks();
                            resetStockButton();
                        }));
            } catch (ConstraintViolationException e) {
                ShowAlert.showError("Terjadi kesalahan saat menghapus stok. Stok mungkin masih terhubung dengan data lain.");
            }
        }
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