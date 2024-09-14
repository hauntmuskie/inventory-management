package com.lestarieragemilang.desktop.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.util.StringConverter;
import org.hibernate.SessionFactory;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.lestarieragemilang.desktop.model.Category;
import com.lestarieragemilang.desktop.model.Stock;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.service.GenericService;
import com.lestarieragemilang.desktop.utils.ClearFields;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.IdGenerator;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.TableUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class StockController extends HibernateUtil {
    @FXML
    private TextField stockIDIncrement, stockQuantityField, stockBuyPriceField, stockSellPriceField, stockSearchField;
    @FXML
    private JFXComboBox<Category> categoryIDDropDown;
    @FXML
    private TableView<Stock> stockTable;
    @FXML
    private TableColumn<Stock, String> stockIDCol, stockOnCategoryIDCol, stockBrandCol, stockTypeCol, stockSizeCol,
            stockWeightCol, stockUnitCol;
    @FXML
    private TableColumn<Stock, Integer> stockQuantityCol;
    @FXML
    private TableColumn<Stock, Double> stockBuyPriceCol, stockSellPriceCol;
    @FXML
    private JFXButton editStockButtonText;

    private GenericService<Stock> stockService;
    private GenericService<Category> categoryService;

    public void initialize() {
        SessionFactory sessionFactory = getSessionFactory();
        stockService = new GenericService<>(new GenericDao<>(Stock.class, sessionFactory), "STK");
        categoryService = new GenericService<>(new GenericDao<>(Category.class, sessionFactory), "CAT");

        initializeCategoryComboBox();
        initializeStockTable();
        loadStocks();
        generateAndSetStockId();

        // Disable the stock ID field
        stockIDIncrement.setDisable(true);
    }

    private void initializeCategoryComboBox() {
        List<Category> categories = categoryService.findAll();
        categoryIDDropDown.setItems(FXCollections.observableArrayList(categories));
        categoryIDDropDown.setConverter(new StringConverter<Category>() {
            @Override
            public String toString(Category category) {
                return category != null
                        ? category.getCategoryId() + " - " + category.getBrand() + " " + category.getProductType()
                        : "";
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
                TableUtils.createColumn("Buy Price", "purchasePrice"),
                TableUtils.createColumn("Sell Price", "sellingPrice"));
        TableUtils.populateTable(stockTable, columns, stockService.findAll());
    }

    private void loadStocks() {
        List<Stock> stocks = stockService.findAll();
        stockTable.setItems(FXCollections.observableArrayList(stocks));
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
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "Stock ID Exists",
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

        stockService.save(stock);
        loadStocks();
        resetStockButton();
    }

    @FXML
    private void resetStockButton() {
        ClearFields.clearFields(
                stockIDIncrement, categoryIDDropDown, stockQuantityField, stockBuyPriceField, stockSellPriceField);
        generateAndSetStockId();
    }

    @FXML
    private void editStockButton() {
        Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "No Stock Selected",
                    "Please select a stock to edit.");
            return;
        }

        selectedStock.setCategory(categoryIDDropDown.getValue());
        selectedStock.setQuantity(Integer.parseInt(stockQuantityField.getText()));
        selectedStock.setPurchasePrice(new BigDecimal(stockBuyPriceField.getText()));
        selectedStock.setSellingPrice(new BigDecimal(stockSellPriceField.getText()));

        stockService.update(selectedStock);
        loadStocks();
        resetStockButton();
    }

    @FXML
    private void removeStockButton() {
        Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
        if (selectedStock == null) {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "No Stock Selected",
                    "Please select a stock to remove.");
            return;
        }

        stockService.delete(selectedStock);
        loadStocks();
        resetStockButton();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = stockSearchField.getText().toLowerCase();
        List<Stock> allStocks = stockService.findAll();
        List<Stock> filteredStocks = allStocks.stream()
                .filter(stock -> stock.getStockId().toLowerCase().contains(searchTerm) ||
                        stock.getCategory().getCategoryId().toLowerCase().contains(searchTerm) ||
                        stock.getCategory().getBrand().toLowerCase().contains(searchTerm) ||
                        stock.getCategory().getProductType().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
        stockTable.setItems(FXCollections.observableArrayList(filteredStocks));
    }
}