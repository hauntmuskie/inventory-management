package com.lestarieragemilang.desktop.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.lestarieragemilang.desktop.model.*;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.service.GenericService;
import com.lestarieragemilang.desktop.utils.ClearFields;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;
import com.lestarieragemilang.desktop.utils.NumberFormatter;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.TableUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
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

    private Integer buyIdValue;
    private Integer sellIdValue;

    private GenericService<Purchasing> purchasingService;
    private GenericService<Sales> salesService;
    private GenericService<Stock> stockService;
    private GenericService<Supplier> supplierService;
    private GenericService<Customer> customerService;

    private ObservableList<Purchasing> pendingPurchases = FXCollections.observableArrayList();
    private ObservableList<Sales> pendingSales = FXCollections.observableArrayList();

    private static int buyId = 1;
    private static int sellId = 1;

    public void initialize() {
        purchasingService = new GenericService<>(new GenericDao<>(Purchasing.class), "PUR", 3);
        salesService = new GenericService<>(new GenericDao<>(Sales.class), "SAL", 3);
        stockService = new GenericService<>(new GenericDao<>(Stock.class), "STK", 3);
        supplierService = new GenericService<>(new GenericDao<>(Supplier.class), "SUP", 3);
        customerService = new GenericService<>(new GenericDao<>(Customer.class), "CUS", 3);

        // Apply number formatting to price and total fields
        NumberFormatter.applyNumberFormat(buyPriceField);
        NumberFormatter.applyNumberFormat(buyTotalPrice);
        NumberFormatter.applyNumberFormat(sellPriceField);
        NumberFormatter.applyNumberFormat(sellTotalPrice);

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
                return stock != null
                        ? stock.getStockId() + " - " + stock.getCategory().getBrand() + " "
                                + stock.getCategory().getProductType()
                        : "";
            }

            @Override
            public Stock fromString(String string) {
                return null;
            }
        });

        sellStockIDDropdown.setConverter(new StringConverter<>() {
            @Override
            public String toString(Stock stock) {
                return stock != null
                        ? stock.getStockId() + " - " + stock.getCategory().getBrand() + " "
                                + stock.getCategory().getProductType()
                        : "";
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
                TableUtils.createFormattedColumn("Price", "price"), // Changed to formatted
                TableUtils.createFormattedColumn("Sub Total", "subTotal"), // Changed to formatted
                TableUtils.createFormattedColumn("Total", "priceTotal")); // Changed to formatted
        buyTable.getColumns().setAll(buyColumns);
        buyTable.setItems(pendingPurchases);

        List<TableColumn<Sales, ?>> sellColumns = List.of(
                TableUtils.createColumn("Brand", "stock.category.brand"),
                TableUtils.createColumn("Type", "stock.category.productType"),
                TableUtils.createColumn("Date", "saleDate"),
                TableUtils.createColumn("Invoice", "invoiceNumber"),
                TableUtils.createColumn("Customer", "customer.customerId"),
                TableUtils.createColumn("Quantity", "quantity"),
                TableUtils.createFormattedColumn("Price", "price"), // Changed to formatted
                TableUtils.createFormattedColumn("Sub Total", "subTotal"), // Changed to formatted
                TableUtils.createFormattedColumn("Total", "priceTotal")); // Changed to formatted
        sellTable.getColumns().setAll(sellColumns);
        sellTable.setItems(pendingSales);
    }

    private void setupAutoFill() {
        buyStockIDDropdown.setOnAction(_ -> {
            Stock selectedStock = buyStockIDDropdown.getValue();
            if (selectedStock != null) {
                buyBrandField.setText(selectedStock.getCategory().getBrand());
                buyTypeField.setText(selectedStock.getCategory().getProductType());
                buyPriceField.setText(NumberFormatter.formatValue(selectedStock.getPurchasePrice()));
            }
        });

        sellStockIDDropdown.setOnAction(_ -> {
            Stock selectedStock = sellStockIDDropdown.getValue();
            if (selectedStock != null) {
                sellBrandField.setText(selectedStock.getCategory().getBrand());
                sellTypeField.setText(selectedStock.getCategory().getProductType());
                sellPriceField.setText(NumberFormatter.formatValue(selectedStock.getSellingPrice()));
            }
        });

        supplierIDDropDown.setOnAction(_ -> {
            Supplier selectedSupplier = supplierIDDropDown.getValue();
            if (selectedSupplier != null) {
                supplierNameField.setText(selectedSupplier.getSupplierName());
            }
        });

        customerIDDropDown.setOnAction(_ -> {
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

        try {
            Stock selectedStock = buyStockIDDropdown.getValue();
            Supplier selectedSupplier = supplierIDDropDown.getValue();
            int quantity = Integer.parseInt(buyTotalField.getText());
            BigDecimal price = new BigDecimal(NumberFormatter.getNumericValue(buyPriceField.getText()));

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
            ShowAlert.showSuccess("Pembelian berhasil ditambahkan ke daftar pending");
        } catch (Exception e) {
            ShowAlert.showError("Gagal menambahkan pembelian: " + e.getMessage());
        }
    }

    @FXML
    private void addSellButton(ActionEvent event) {
        if (!validateSellFields()) {
            return;
        }

        try {
            Stock selectedStock = sellStockIDDropdown.getValue();
            Customer selectedCustomer = customerIDDropDown.getValue();
            int quantity = Integer.parseInt(sellTotalField.getText());
            BigDecimal price = new BigDecimal(NumberFormatter.getNumericValue(sellPriceField.getText()));

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
            ShowAlert.showSuccess("Penjualan berhasil ditambahkan ke daftar pending");
        } catch (Exception e) {
            ShowAlert.showError("Gagal menambahkan penjualan: " + e.getMessage());
        }
    }

    private String generateInvoiceNumber(String prefix, Stock stock) {
        LocalDate currentDate = LocalDate.now();
        return String.format("%s-%s-%s-%03d", prefix, stock.getStockId(), currentDate, getNextInvoiceSequence(prefix));
    }

    private int getNextInvoiceSequence(String prefix) {
        List<?> transactions = prefix.equals("PUR") ? pendingPurchases : pendingSales;
        return transactions.size() + 1;
    }

    // Add this method to generate a unique timestamp-based invoice number
    private String generateUniqueInvoiceNumber(String prefix, Stock stock) {
        LocalDate currentDate = LocalDate.now();
        long timestamp = System.currentTimeMillis();
        return String.format("%s-%s-%s-%d", prefix, stock.getStockId(), currentDate, timestamp % 1000);
    }

    @FXML
    private void confirmBuyButton(ActionEvent event) throws MalformedURLException, URISyntaxException {
        if (!ShowAlert.showYesNo("Konfirmasi Pembelian", "Apakah Anda yakin ingin mengkonfirmasi semua pembelian?")) {
            return;
        }

        if (tabPane.getSelectionModel().getSelectedIndex() == 0) {
            try {
                List<Purchasing> purchasingList = new ArrayList<>(buyTable.getItems());
                boolean success = true;

                for (Purchasing purchasing : purchasingList) {
                    purchasing.setInvoiceNumber(generateUniqueInvoiceNumber("PUR", purchasing.getStock()));

                    try {
                        purchasingService.save(purchasing);
                        updateStockQuantity(purchasing.getStock(), purchasing.getQuantity(), true);
                    } catch (Exception e) {
                        success = false;
                        ShowAlert.showError("Gagal mengkonfirmasi pembelian: " + e.getMessage());
                        break;
                    }
                }

                if (success) {
                    buyTable.getItems().clear();
                    buyIdValue = buyId++;
                    buyInvoiceNumber.setText(String.format("TRX-%05d", buyIdValue));
                    updateBuyTotalPrice();

                    printJasperBuyList();

                    ShowAlert.showSuccess("Pembelian berhasil dikonfirmasi");
                }
            } catch (Exception e) {
                e.printStackTrace();
                ShowAlert.showError("Gagal mengkonfirmasi pembelian: " + e.getMessage());
            }
        }
    }

    @FXML
    private void confirmSellButton(ActionEvent event) throws MalformedURLException, URISyntaxException {
        if (!ShowAlert.showYesNo("Konfirmasi Penjualan", "Apakah Anda yakin ingin mengkonfirmasi semua penjualan?")) {
            return;
        }

        if (tabPane.getSelectionModel().getSelectedIndex() == 1) {
            try {
                List<Sales> salesList = new ArrayList<>(sellTable.getItems());
                boolean success = true;

                for (Sales sale : salesList) {
                    // Generate unique invoice number for each sale
                    sale.setInvoiceNumber(generateUniqueInvoiceNumber("SAL", sale.getStock()));

                    try {
                        salesService.save(sale);
                        updateStockQuantity(sale.getStock(), sale.getQuantity(), false);
                    } catch (Exception e) {
                        success = false;
                        ShowAlert.showError("Gagal mengkonfirmasi penjualan: " + e.getMessage());
                        break;
                    }
                }

                if (success) {
                    sellTable.getItems().clear();
                    sellIdValue = sellId++;
                    sellInvoiceNumber.setText(String.format("TRX-%05d", sellIdValue));
                    updateSellTotalPrice();

                    printJasperSellList();

                    ShowAlert.showSuccess("Penjualan berhasil dikonfirmasi");
                }
            } catch (Exception e) {
                e.printStackTrace();
                ShowAlert.showError("Gagal mengkonfirmasi penjualan: " + e.getMessage());
            }
        }
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
            ShowAlert.showWarning("Silakan pilih pembelian yang akan diedit");
            return;
        }

        buyDate.setValue(selectedPurchase.getPurchaseDate());
        buyInvoiceNumber.setText(selectedPurchase.getInvoiceNumber());
        buyStockIDDropdown.setValue(selectedPurchase.getStock());
        supplierIDDropDown.setValue(selectedPurchase.getSupplier());
        buyTotalField.setText(String.valueOf(selectedPurchase.getQuantity()));
        buyPriceField.setText(NumberFormatter.formatValue(selectedPurchase.getPrice()));

        pendingPurchases.remove(selectedPurchase);
        updateBuyTotalPrice();
    }

    @FXML
    private void editSellButton(ActionEvent event) {
        Sales selectedSale = sellTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            ShowAlert.showWarning("Silakan pilih penjualan yang akan diedit");
            return;
        }

        sellDate.setValue(selectedSale.getSaleDate());
        sellInvoiceNumber.setText(selectedSale.getInvoiceNumber());
        sellStockIDDropdown.setValue(selectedSale.getStock());
        customerIDDropDown.setValue(selectedSale.getCustomer());
        sellTotalField.setText(String.valueOf(selectedSale.getQuantity()));
        sellPriceField.setText(NumberFormatter.formatValue(selectedSale.getPrice()));

        pendingSales.remove(selectedSale);
        updateSellTotalPrice();
    }

    @FXML
    private void removeBuyButton(ActionEvent event) {
        Purchasing selectedPurchase = buyTable.getSelectionModel().getSelectedItem();
        if (selectedPurchase == null) {
            ShowAlert.showWarning("Silakan pilih pembelian yang akan dihapus");
            return;
        }

        if (ShowAlert.showYesNo("Hapus Pembelian", "Apakah Anda yakin ingin menghapus pembelian ini?")) {
            pendingPurchases.remove(selectedPurchase);
            updateBuyTotalPrice();
            ShowAlert.showSuccess("Pembelian berhasil dihapus");
        }
    }

    @FXML
    private void removeSellButton(ActionEvent event) {
        Sales selectedSale = sellTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            ShowAlert.showWarning("Silakan pilih penjualan yang akan dihapus");
            return;
        }

        if (ShowAlert.showYesNo("Hapus Penjualan", "Apakah Anda yakin ingin menghapus penjualan ini?")) {
            pendingSales.remove(selectedSale);
            updateSellTotalPrice();
            ShowAlert.showSuccess("Penjualan berhasil dihapus");
        }
    }

    private void updateBuyTotalPrice() {
        BigDecimal total = pendingPurchases.stream()
                .map(Purchasing::getPriceTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        buyTotalPrice.setText(NumberFormatter.formatValue(total));
    }

    private void updateSellTotalPrice() {
        BigDecimal total = pendingSales.stream()
                .map(Sales::getPriceTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        sellTotalPrice.setText(NumberFormatter.formatValue(total));
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
            ShowAlert.showValidationError("Mohon lengkapi semua field yang diperlukan");
            return false;
        }
        return true;
    }

    private boolean validateSellFields() {
        if (sellDate.getValue() == null || sellStockIDDropdown.getValue() == null ||
                customerIDDropDown.getValue() == null || sellTotalField.getText().isEmpty() ||
                sellPriceField.getText().isEmpty()) {
            ShowAlert.showValidationError("Mohon lengkapi semua field yang diperlukan");
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

    private void printJasperBuyList() throws MalformedURLException, URISyntaxException {
        String path = "/com/lestarieragemilang/desktop/jasper/purchasing.jasper";
        URL url = TransactionController.class.getResource(path).toURI().toURL();
        try {
            JasperLoader loader = new JasperLoader();

            loader.showJasperReportBuy(
                    url,
                    buyIdValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printJasperSellList() throws MalformedURLException, URISyntaxException {
        String path = "/com/lestarieragemilang/desktop/jasper/sales.jasper";
        URL url = TransactionController.class.getResource(path).toURI().toURL();
        try {
            JasperLoader loader = new JasperLoader();

            loader.showJasperReportSell(
                    url,
                    sellIdValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
