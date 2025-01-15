package com.lestarieragemilang.desktop.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
        this.stockService = new GenericService<>(new GenericDao<>(Stock.class), "BRG", 3);
        this.categoryService = new GenericService<>(new GenericDao<>(Category.class), "KTG", 3);
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
                TableUtils.createColumn("Kode Barang", "stockId"),
                TableUtils.createColumn("Kode Kategori", "category.categoryId"),
                TableUtils.createColumn("Merek", "category.brand"),
                TableUtils.createColumn("Jenis", "category.productType"),
                TableUtils.createColumn("Ukuran", "category.size"),
                TableUtils.createColumn("Berat", "category.weight"),
                TableUtils.createColumn("Satuan", "category.weightUnit"),
                TableUtils.createColumn("Jumlah", "quantity"),
                TableUtils.createFormattedColumn("Harga Beli", "purchasePrice"),
                TableUtils.createFormattedColumn("Harga Jual", "sellingPrice"));
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
            newStockId = IdGenerator.generateRandomId("BRG", 1000);
        } while (stockIdExists(newStockId));

        stockIDIncrement.setText(newStockId);
    }

    private boolean stockIdExists(String stockId) {
        return stockService.findAll().stream()
                .anyMatch(stock -> stock.getStockId().equals(stockId));
    }

    @FXML
    private void addStockButton() {
        if (!ShowAlert.showYesNo("Konfirmasi Tambah", "Apakah Anda yakin ingin menambah barang ini?")) {
            return;
        }

        String stockId = stockIDIncrement.getText();
        if (stockIdExists(stockId)) {
            ShowAlert.showWarning("Kode barang sudah ada di database.");
            generateAndSetStockId();
            return;
        }

        try {
            Stock stock = new Stock();
            stock.setStockId(stockId);
            stock.setCategory(categoryIDDropDown.getValue());
            stock.setQuantity(Integer.parseInt(stockQuantityField.getText()));
            stock.setPurchasePrice(new BigDecimal(NumberFormatter.getNumericValue(stockBuyPriceField.getText())));
            stock.setSellingPrice(new BigDecimal(NumberFormatter.getNumericValue(stockSellPriceField.getText())));

            CompletableFuture.runAsync(() -> stockService.save(stock))
                    .thenRun(() -> Platform.runLater(() -> {
                        ShowAlert.showSuccess("Data barang berhasil ditambahkan");
                        loadStocks();
                        resetStockButton();
                    }))
                    .exceptionally(e -> {
                        Platform.runLater(
                                () -> ShowAlert.showError("Gagal menambahkan data barang: " + e.getMessage()));
                        return null;
                    });
        } catch (NumberFormatException e) {
            ShowAlert.showValidationError("Mohon periksa format input angka");
        } catch (Exception e) {
            ShowAlert.showError("Terjadi kesalahan: " + e.getMessage());
        }
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
            ShowAlert.showWarning("Silakan pilih barang yang akan diubah");
            return;
        }

        JFXComboBox<Category> categoryComboBox = new JFXComboBox<>(categoryIDDropDown.getItems());

        for (Category category : categoryIDDropDown.getItems()) {
            if (category.getId().equals(selectedStock.getCategory().getId())) {
                categoryComboBox.setValue(category);
                break;
            }
        }

        GenericEditPopup.create(Stock.class)
                .withTitle("Ubah Barang")
                .forItem(selectedStock)
                .addField("Kode Barang", new TextField(selectedStock.getStockId()), true)
                .addField("Kategori", categoryComboBox) // Use the pre-set categoryComboBox
                .addField("Jumlah", new TextField(String.valueOf(selectedStock.getQuantity())))
                .addField("Harga Beli", createFormattedTextField(selectedStock.getPurchasePrice()))
                .addField("Harga Jual", createFormattedTextField(selectedStock.getSellingPrice()))
                .onSave((stock, fields) -> {
                    if (!ShowAlert.showYesNo("Konfirmasi Ubah", "Apakah Anda yakin ingin mengubah data barang ini?")) {
                        return;
                    }
                    try {
                        stock.setCategory(((JFXComboBox<Category>) fields.get(1)).getValue());
                        stock.setQuantity(Integer.parseInt(((TextField) fields.get(2)).getText()));
                        stock.setPurchasePrice(
                                new BigDecimal(NumberFormatter.getNumericValue(((TextField) fields.get(3)).getText())));
                        stock.setSellingPrice(
                                new BigDecimal(NumberFormatter.getNumericValue(((TextField) fields.get(4)).getText())));
                        CompletableFuture.runAsync(() -> {
                            stockService.update(stock);
                            Platform.runLater(() -> {
                                ShowAlert.showSuccess("Data barang berhasil diubah");
                                loadStocks();
                            });
                        }).exceptionally(e -> {
                            Platform.runLater(
                                    () -> ShowAlert.showError("Gagal mengubah data barang: " + e.getMessage()));
                            return null;
                        });
                    } catch (NumberFormatException e) {
                        ShowAlert.showValidationError("Mohon periksa format input angka");
                    } catch (Exception e) {
                        ShowAlert.showError("Terjadi kesalahan: " + e.getMessage());
                    }
                })
                .afterSave(this::resetStockButton)
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
            ShowAlert.showWarning("Silakan pilih barang yang akan dihapus");
            return;
        }

        if (!stockService.canDelete(selectedStock)) {
            ShowAlert.showError("Barang tidak dapat dihapus karena masih terhubung dengan data lain");
            return;
        }

        if (!ShowAlert.showYesNo("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus barang ini?")) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                stockService.delete(selectedStock);
                Platform.runLater(() -> {
                    ShowAlert.showSuccess("Data barang berhasil dihapus");
                    loadStocks();
                    resetStockButton();
                });
            } catch (ConstraintViolationException e) {
                Platform.runLater(() -> ShowAlert.showError(
                        "Terjadi kesalahan saat menghapus barang. Barang mungkin masih terhubung dengan data lain."));
            } catch (Exception e) {
                Platform.runLater(() -> ShowAlert.showError("Gagal menghapus data barang: " + e.getMessage()));
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