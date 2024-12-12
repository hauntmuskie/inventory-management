package com.lestarieragemilang.desktop.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.lestarieragemilang.desktop.model.*;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.service.GenericService;
import com.lestarieragemilang.desktop.utils.ClearFields;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.TableUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionController extends HibernateUtil {
    @FXML
    private TableColumn<Purchasing, String> buyBrandCol, buyInvoiceCol, buyOnSupplierNameCol, buyTypeCol;
    @FXML
    private TextField buyBrandField, buyInvoiceNumber, buyPriceField, buyTotalField, buyTotalPrice, buyTypeField,
            supplierNameField;
    @FXML
    private DatePicker buyDate;
    @FXML
    private TableColumn<Purchasing, LocalDate> buyDateCol;
    @FXML
    private TableColumn<Purchasing, BigDecimal> buyPriceCol, buySubTotalCol, buyTotalCol;
    @FXML
    private JFXComboBox<Stock> buyStockIDDropdown;
    @FXML
    private TableView<Purchasing> buyTable;
    @FXML
    private JFXComboBox<Supplier> supplierIDDropDown;
    @FXML
    private JFXButton editBuyButtonText, editSellButtonText;
    @FXML
    private TableColumn<Sales, String> sellBrandCol, sellInvoiceCol, sellOnCustomerNameCol, sellTypeCol;
    @FXML
    private TextField sellBrandField, sellInvoiceNumber, sellPriceField, sellTotalField, sellTotalPrice, sellTypeField,
            customerNameField;
    @FXML
    private DatePicker sellDate;
    @FXML
    private TableColumn<Sales, LocalDate> sellDateCol;
    @FXML
    private TableColumn<Sales, BigDecimal> sellPriceCol, sellSubTotalCol, sellTotalCol;
    @FXML
    private JFXComboBox<Stock> sellStockIDDropdown;
    @FXML
    private TableView<Sales> sellTable;
    @FXML
    private JFXComboBox<Customer> customerIDDropDown;
    @FXML
    private TabPane tabPane;
    @FXML
    private TextField transactionBuySearchField, transactionSellSearchField;

    private GenericService<Purchasing> purchasingService;
    private GenericService<Sales> salesService;
    private GenericService<Stock> stockService;
    private GenericService<Supplier> supplierService;
    private GenericService<Customer> customerService;

    private ObservableList<Purchasing> pendingPurchases = FXCollections.observableArrayList();
    private ObservableList<Sales> pendingSales = FXCollections.observableArrayList();

    public void initialize() {
        purchasingService = new GenericService<>(new GenericDao<>(Purchasing.class), "PUR", 3);
        salesService = new GenericService<>(new GenericDao<>(Sales.class), "SAL", 3);
        stockService = new GenericService<>(new GenericDao<>(Stock.class), "STK", 3);
        supplierService = new GenericService<>(new GenericDao<>(Supplier.class), "SUP", 3);
        customerService = new GenericService<>(new GenericDao<>(Customer.class), "CUS", 3);

        initializeComboBoxes();
        initializeTables();
        setupAutoFill();

        buyDate.setValue(LocalDate.now());
        sellDate.setValue(LocalDate.now());
    }

    private void initializeComboBoxes() {
        List<Stock> stocks = stockService.findAll();
        buyStockIDDropdown.setItems(FXCollections.observableArrayList(stocks));
        sellStockIDDropdown.setItems(FXCollections.observableArrayList(stocks));

        List<Supplier> suppliers = supplierService.findAll();
        supplierIDDropDown.setItems(FXCollections.observableArrayList(suppliers));

        List<Customer> customers = customerService.findAll();
        customerIDDropDown.setItems(FXCollections.observableArrayList(customers));

        // Set default values if lists are not empty
        if (!stocks.isEmpty()) {
            buyStockIDDropdown.setValue(stocks.get(0));
            sellStockIDDropdown.setValue(stocks.get(0));
        }
        
        if (!suppliers.isEmpty()) {
            supplierIDDropDown.setValue(suppliers.get(0));
        }
        
        if (!customers.isEmpty()) {
            customerIDDropDown.setValue(customers.get(0));
        }

        setupComboBoxConverters();
    }

    private void setupComboBoxConverters() {
        buyStockIDDropdown.setConverter(new StringConverter<>() {
            @Override
            public String toString(Stock stock) {
                return stock != null ? stock.getStockId() + " - " + stock.getCategory().getBrand() + " " + stock.getCategory().getProductType() : "";
            }

            @Override
            public Stock fromString(String string) {
                return null;
            }
        });

        sellStockIDDropdown.setConverter(new StringConverter<>() {
            @Override
            public String toString(Stock stock) {
                return stock != null ? stock.getStockId() + " - " + stock.getCategory().getBrand() + " " + stock.getCategory().getProductType() : "";
            }

            @Override
            public Stock fromString(String string) {
                return null;
            }
        });

        supplierIDDropDown.setConverter(new StringConverter<>() {
            @Override
            public String toString(Supplier supplier) {
                return supplier != null ? supplier.getSupplierId() + " - " + supplier.getSupplierName() : "";
            }

            @Override
            public Supplier fromString(String string) {
                return null;
            }
        });

        customerIDDropDown.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                return customer != null ? customer.getCustomerId() + " - " + customer.getCustomerName() : "";
            }

            @Override
            public Customer fromString(String string) {
                return null;
            }
        });
    }

    private void initializeTables() {
        List<TableColumn<Purchasing, ?>> buyColumns = List.of(
                TableUtils.createColumn("Brand", "stock.category.brand"),
                TableUtils.createColumn("Type", "stock.category.productType"),
                TableUtils.createColumn("Date", "purchaseDate"),
                TableUtils.createColumn("Invoice", "invoiceNumber"),
                TableUtils.createColumn("Supplier", "supplier.supplierId"),
                TableUtils.createColumn("Quantity", "quantity"),
                TableUtils.createFormattedColumn("Price", "price"),         // Changed to formatted
                TableUtils.createFormattedColumn("Sub Total", "subTotal"), // Changed to formatted
                TableUtils.createFormattedColumn("Total", "priceTotal"));  // Changed to formatted
        buyTable.getColumns().setAll(buyColumns);
        buyTable.setItems(pendingPurchases);

        List<TableColumn<Sales, ?>> sellColumns = List.of(
                TableUtils.createColumn("Brand", "stock.category.brand"),
                TableUtils.createColumn("Type", "stock.category.productType"),
                TableUtils.createColumn("Date", "saleDate"),
                TableUtils.createColumn("Invoice", "invoiceNumber"),
                TableUtils.createColumn("Customer", "customer.customerId"),
                TableUtils.createColumn("Quantity", "quantity"),
                TableUtils.createFormattedColumn("Price", "price"),         // Changed to formatted
                TableUtils.createFormattedColumn("Sub Total", "subTotal"), // Changed to formatted
                TableUtils.createFormattedColumn("Total", "priceTotal"));  // Changed to formatted
        sellTable.getColumns().setAll(sellColumns);
        sellTable.setItems(pendingSales);
    }

    private void setupAutoFill() {
        buyStockIDDropdown.setOnAction(event -> {
            Stock selectedStock = buyStockIDDropdown.getValue();
            if (selectedStock != null) {
                buyBrandField.setText(selectedStock.getCategory().getBrand());
                buyTypeField.setText(selectedStock.getCategory().getProductType());
                buyPriceField.setText(selectedStock.getPurchasePrice().toString());
            }
        });

        sellStockIDDropdown.setOnAction(event -> {
            Stock selectedStock = sellStockIDDropdown.getValue();
            if (selectedStock != null) {
                sellBrandField.setText(selectedStock.getCategory().getBrand());
                sellTypeField.setText(selectedStock.getCategory().getProductType());
                sellPriceField.setText(selectedStock.getSellingPrice().toString());
            }
        });

        supplierIDDropDown.setOnAction(event -> {
            Supplier selectedSupplier = supplierIDDropDown.getValue();
            if (selectedSupplier != null) {
                supplierNameField.setText(selectedSupplier.getSupplierName());
            }
        });

        customerIDDropDown.setOnAction(event -> {
            Customer selectedCustomer = customerIDDropDown.getValue();
            if (selectedCustomer != null) {
                customerNameField.setText(selectedCustomer.getCustomerName());
            }
        });
    }

    @FXML
    private void addBuyButton(ActionEvent event) {
        if (!validateBuyFields()) {
            return;
        }

        Stock selectedStock = buyStockIDDropdown.getValue();
        Supplier selectedSupplier = supplierIDDropDown.getValue();
        int quantity = Integer.parseInt(buyTotalField.getText());
        BigDecimal price = new BigDecimal(buyPriceField.getText());

        Purchasing purchasing = new Purchasing();
        purchasing.setPurchaseDate(buyDate.getValue());
        purchasing.setInvoiceNumber(generateInvoiceNumber("PUR", selectedStock));
        purchasing.setStock(selectedStock);
        purchasing.setSupplier(selectedSupplier);
        purchasing.setQuantity(quantity);
        purchasing.setPrice(price);
        purchasing.setSubTotal(price.multiply(BigDecimal.valueOf(quantity)));
        purchasing.setPriceTotal(purchasing.getSubTotal());

        pendingPurchases.add(purchasing);
        updateBuyTotalPrice();
        resetBuyButton(event);
    }

    @FXML
    private void addSellButton(ActionEvent event) {
        if (!validateSellFields()) {
            return;
        }

        Stock selectedStock = sellStockIDDropdown.getValue();
        Customer selectedCustomer = customerIDDropDown.getValue();
        int quantity = Integer.parseInt(sellTotalField.getText());
        BigDecimal price = new BigDecimal(sellPriceField.getText());

        Sales sales = new Sales();
        sales.setSaleDate(sellDate.getValue());
        sales.setInvoiceNumber(generateInvoiceNumber("SAL", selectedStock));
        sales.setStock(selectedStock);
        sales.setCustomer(selectedCustomer);
        sales.setQuantity(quantity);
        sales.setPrice(price);
        sales.setSubTotal(price.multiply(BigDecimal.valueOf(quantity)));
        sales.setPriceTotal(sales.getSubTotal());

        pendingSales.add(sales);
        updateSellTotalPrice();
        resetSellButton(event);
    }

    private String generateInvoiceNumber(String prefix, Stock stock) {
        LocalDate currentDate = LocalDate.now();
        return String.format("%s-%s-%s-%03d", prefix, stock.getStockId(), currentDate, getNextInvoiceSequence(prefix));
    }

    private int getNextInvoiceSequence(String prefix) {
        List<?> transactions = prefix.equals("PUR") ? pendingPurchases : pendingSales;
        return transactions.size() + 1;
    }

    @FXML
    private void confirmBuyButton(ActionEvent event) {
        for (Purchasing purchasing : pendingPurchases) {
            purchasingService.save(purchasing);
            updateStockQuantity(purchasing.getStock(), purchasing.getQuantity(), true);
        }
        pendingPurchases.clear();
        updateBuyTotalPrice();
        ShowAlert.showAlert(
                AlertType.INFORMATION,
                "Purchases confirmed successfully.",
                "Purchases confirmed successfully.");
    }

    @FXML
    private void confirmSellButton(ActionEvent event) {
        for (Sales sale : pendingSales) {
            salesService.save(sale);
            updateStockQuantity(sale.getStock(), sale.getQuantity(), false);
        }
        pendingSales.clear();
        updateSellTotalPrice();
        ShowAlert.showAlert(
                AlertType.INFORMATION,
                "Sales confirmed successfully.",
                "Sales confirmed successfully.");
    }

    private void updateStockQuantity(Stock stock, int quantity, boolean isIncrease) {
        int currentQuantity = stock.getQuantity();
        stock.setQuantity(isIncrease ? currentQuantity + quantity : currentQuantity - quantity);
        stockService.update(stock);
    }

    @FXML
    private void editBuyButton(ActionEvent event) {
        Purchasing selectedPurchase = buyTable.getSelectionModel().getSelectedItem();
        if (selectedPurchase == null) {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "No Purchase Selected",
                    "Please select a purchase to edit.");
            return;
        }

        buyDate.setValue(selectedPurchase.getPurchaseDate());
        buyInvoiceNumber.setText(selectedPurchase.getInvoiceNumber());
        buyStockIDDropdown.setValue(selectedPurchase.getStock());
        supplierIDDropDown.setValue(selectedPurchase.getSupplier());
        buyTotalField.setText(String.valueOf(selectedPurchase.getQuantity()));
        buyPriceField.setText(selectedPurchase.getPrice().toString());

        pendingPurchases.remove(selectedPurchase);
        updateBuyTotalPrice();
    }

    @FXML
    private void editSellButton(ActionEvent event) {
        Sales selectedSale = sellTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "No Sale Selected",
                    "Please select a sale to edit.");
            return;
        }

        sellDate.setValue(selectedSale.getSaleDate());
        sellInvoiceNumber.setText(selectedSale.getInvoiceNumber());
        sellStockIDDropdown.setValue(selectedSale.getStock());
        customerIDDropDown.setValue(selectedSale.getCustomer());
        sellTotalField.setText(String.valueOf(selectedSale.getQuantity()));
        sellPriceField.setText(selectedSale.getPrice().toString());

        pendingSales.remove(selectedSale);
        updateSellTotalPrice();
    }

    @FXML
    private void removeBuyButton(ActionEvent event) {
        Purchasing selectedPurchase = buyTable.getSelectionModel().getSelectedItem();
        if (selectedPurchase == null) {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "No Purchase Selected",
                    "Please select a purchase to remove.");
            return;
        }

        pendingPurchases.remove(selectedPurchase);
        updateBuyTotalPrice();
    }

    @FXML
    private void removeSellButton(ActionEvent event) {
        Sales selectedSale = sellTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "No Sale Selected",
                    "Please select a sale to remove.");
            return;
        }

        pendingSales.remove(selectedSale);
        updateSellTotalPrice();
    }

    private void updateBuyTotalPrice() {
        BigDecimal total = pendingPurchases.stream()
                .map(Purchasing::getPriceTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        buyTotalPrice.setText(TableUtils.formatCurrency(total));
    }

    private void updateSellTotalPrice() {
        BigDecimal total = pendingSales.stream()
                .map(Sales::getPriceTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        sellTotalPrice.setText(TableUtils.formatCurrency(total));
    }

    @FXML
    private void resetBuyButton(ActionEvent event) {
        ClearFields.clearFields(
                buyDate, buyInvoiceNumber, buyStockIDDropdown, supplierIDDropDown, buyTotalField, buyPriceField,
                buyBrandField, buyTypeField, supplierNameField);
    }

    @FXML
    private void resetSellButton(ActionEvent event) {
        ClearFields.clearFields(
                sellDate, sellInvoiceNumber, sellStockIDDropdown, customerIDDropDown, sellTotalField, sellPriceField,
                sellBrandField, sellTypeField, customerNameField);
    }

    private boolean validateBuyFields() {
        if (buyDate.getValue() == null || buyStockIDDropdown.getValue() == null ||
                supplierIDDropDown.getValue() == null || buyTotalField.getText().isEmpty() ||
                buyPriceField.getText().isEmpty()) {
            ShowAlert.showAlert(
                    AlertType.INFORMATION,
                    "Please fill in all required fields.",
                    "Please fill in all required fields.");
            return false;
        }
        return true;
    }

    private boolean validateSellFields() {
        if (sellDate.getValue() == null || sellStockIDDropdown.getValue() == null ||
                customerIDDropDown.getValue() == null || sellTotalField.getText().isEmpty() ||
                sellPriceField.getText().isEmpty()) {
            ShowAlert.showAlert(
                    AlertType.INFORMATION,
                    "Please fill in all required fields.",
                    "Please fill in all required fields."
            );
            return false;
        }
        return true;
    }

    @FXML
    private void searchDataBuyAction(ActionEvent event) {
        String searchTerm = transactionBuySearchField.getText().toLowerCase();
        List<Purchasing> filteredPurchases = pendingPurchases.stream()
                .filter(purchase -> purchase.getInvoiceNumber().toLowerCase().contains(searchTerm) ||
                        purchase.getStock().getStockId().toLowerCase().contains(searchTerm) ||
                        purchase.getSupplier().getSupplierId().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
        buyTable.setItems(FXCollections.observableArrayList(filteredPurchases));
    }

    @FXML
    private void searchDataSellAction(ActionEvent event) {
        String searchTerm = transactionSellSearchField.getText().toLowerCase();
        List<Sales> filteredSales = pendingSales.stream()
                .filter(sale -> sale.getInvoiceNumber().toLowerCase().contains(searchTerm) ||
                        sale.getStock().getStockId().toLowerCase().contains(searchTerm) ||
                        sale.getCustomer().getCustomerId().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
        sellTable.setItems(FXCollections.observableArrayList(filteredSales));
    }
}