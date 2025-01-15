package com.lestarieragemilang.desktop.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.lestarieragemilang.desktop.model.*;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.service.GenericService;
import com.lestarieragemilang.desktop.utils.ClearFields;
import com.lestarieragemilang.desktop.utils.GenericEditPopup;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;
import com.lestarieragemilang.desktop.utils.NumberFormatter;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.TableUtils;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.hibernate.Session;
import org.hibernate.Transaction;

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

    @SuppressWarnings("unused")
    private GenericService<Purchasing> purchasingService;

    @SuppressWarnings("unused")
    private GenericService<Sales> salesService;

    private GenericService<Stock> stockService;
    private GenericService<Supplier> supplierService;
    private GenericService<Customer> customerService;

    private ObservableList<Purchasing> pendingPurchases = FXCollections.observableArrayList();
    private ObservableList<Sales> pendingSales = FXCollections.observableArrayList();

    private static int buyId = 1;
    private static int sellId = 1;

    private String currentPendingBuyInvoice;
    private String currentPendingSellInvoice;

    String finalInvoiceNumber;

    public void initialize() {
        purchasingService = new GenericService<>(new GenericDao<>(Purchasing.class), "PUR", 3);
        salesService = new GenericService<>(new GenericDao<>(Sales.class), "SAL", 3);
        stockService = new GenericService<>(new GenericDao<>(Stock.class), "STK", 3);
        supplierService = new GenericService<>(new GenericDao<>(Supplier.class), "SUP", 3);
        customerService = new GenericService<>(new GenericDao<>(Customer.class), "CUS", 3);

        NumberFormatter.applyNumberFormat(buyPriceField);
        NumberFormatter.applyNumberFormat(buyTotalPrice);
        NumberFormatter.applyNumberFormat(sellPriceField);
        NumberFormatter.applyNumberFormat(sellTotalPrice);

        initializeComboBoxes();
        initializeTables();
        setupAutoFill();

        buyDate.setValue(LocalDate.now());
        sellDate.setValue(LocalDate.now());

        currentPendingBuyInvoice = generatePendingInvoiceNumber("BLI");
        currentPendingSellInvoice = generatePendingInvoiceNumber("JUL");
    }

    private void initializeComboBoxes() {
        List<Stock> stocks = stockService.findAll();
        buyStockIDDropdown.setItems(FXCollections.observableArrayList(stocks));
        sellStockIDDropdown.setItems(FXCollections.observableArrayList(stocks));

        List<Supplier> suppliers = supplierService.findAll();
        supplierIDDropDown.setItems(FXCollections.observableArrayList(suppliers));

        List<Customer> customers = customerService.findAll();
        customerIDDropDown.setItems(FXCollections.observableArrayList(customers));

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
                TableUtils.createColumn("Merek", "stock.category.brand"),
                TableUtils.createColumn("Tipe", "stock.category.productType"),
                TableUtils.createColumn("Tanggal", "purchaseDate"),
                TableUtils.createColumn("No Faktur", "invoiceNumber"),
                TableUtils.createColumn("Pemasok", "supplier.supplierId"),
                TableUtils.createColumn("Jumlah", "quantity"),
                TableUtils.createFormattedColumn("Harga", "price"),
                TableUtils.createFormattedColumn("Sub Total", "subTotal"),
                TableUtils.createFormattedColumn("Total", "priceTotal"));
        buyTable.getColumns().setAll(buyColumns);
        buyTable.setItems(pendingPurchases);

        List<TableColumn<Sales, ?>> sellColumns = List.of(
                TableUtils.createColumn("Merek", "stock.category.brand"),
                TableUtils.createColumn("Tipe", "stock.category.productType"),
                TableUtils.createColumn("Tanggal", "saleDate"),
                TableUtils.createColumn("No Faktur", "invoiceNumber"),
                TableUtils.createColumn("Pelanggan", "customer.customerId"),
                TableUtils.createColumn("Jumlah", "quantity"),
                TableUtils.createFormattedColumn("Harga", "price"),
                TableUtils.createFormattedColumn("Sub Total", "subTotal"),
                TableUtils.createFormattedColumn("Total", "priceTotal"));
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
        if (!ShowAlert.showYesNo("Konfirmasi Tambah", "Apakah Anda yakin ingin menambah pembelian ini?")) {
            return;
        }

        if (!validateBuyFields()) {
            return;
        }

        try {
            Stock selectedStock = buyStockIDDropdown.getValue();
            Supplier selectedSupplier = supplierIDDropDown.getValue();

            int quantity;
            try {
                quantity = Integer.parseInt(buyTotalField.getText());
                if (quantity <= 0) {
                    ShowAlert.showValidationError("Jumlah harus lebih besar dari 0");
                    return;
                }
            } catch (NumberFormatException e) {
                ShowAlert.showValidationError("Format jumlah tidak valid");
                return;
            }

            BigDecimal price;
            try {
                price = new BigDecimal(NumberFormatter.getNumericValue(buyPriceField.getText()));
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    ShowAlert.showValidationError("Harga harus lebih besar dari 0");
                    return;
                }
            } catch (Exception e) {
                ShowAlert.showValidationError("Format harga tidak valid");
                return;
            }

            Purchasing purchasing = new Purchasing();
            purchasing.setPurchaseDate(buyDate.getValue());
            purchasing.setInvoiceNumber(currentPendingBuyInvoice);
            purchasing.setStock(selectedStock);
            purchasing.setSupplier(selectedSupplier);
            purchasing.setQuantity(quantity);
            purchasing.setPrice(price);

            BigDecimal subTotal = price.multiply(BigDecimal.valueOf(quantity));
            purchasing.setSubTotal(subTotal);

            BigDecimal total = subTotal;
            purchasing.setPriceTotal(total);

            purchasing.setBrand(selectedStock.getCategory().getBrand());
            purchasing.setType(selectedStock.getCategory().getProductType());

            pendingPurchases.add(purchasing);
            updateBuyTotalPrice();
            resetBuyButton(event);
        } catch (Exception e) {
            ShowAlert.showError("Gagal menambahkan pembelian: " + e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    @FXML
    private void addSellButton(ActionEvent event) {
        if (!ShowAlert.showYesNo("Konfirmasi Tambah", "Apakah Anda yakin ingin menambah penjualan ini?")) {
            return;
        }

        if (!validateSellFields()) {
            return;
        }

        try {
            Stock selectedStock = sellStockIDDropdown.getValue();
            Customer selectedCustomer = customerIDDropDown.getValue();

            int quantity;
            try {
                quantity = Integer.parseInt(sellTotalField.getText());
                if (quantity <= 0) {
                    ShowAlert.showValidationError("Jumlah harus lebih besar dari 0");
                    return;
                }
                if (quantity > selectedStock.getQuantity()) {
                    ShowAlert
                            .showValidationError("Stok tidak mencukupi. Stok tersedia: " + selectedStock.getQuantity());
                    return;
                }
            } catch (NumberFormatException e) {
                ShowAlert.showValidationError("Format jumlah tidak valid");
                return;
            }

            BigDecimal price;
            try {
                price = new BigDecimal(NumberFormatter.getNumericValue(sellPriceField.getText()));
                if (price.compareTo(BigDecimal.ZERO) <= 0) {
                    ShowAlert.showValidationError("Harga harus lebih besar dari 0");
                    return;
                }
            } catch (Exception e) {
                ShowAlert.showValidationError("Format harga tidak valid");
                return;
            }

            Sales sales = new Sales();
            sales.setSaleDate(sellDate.getValue());
            sales.setInvoiceNumber(currentPendingSellInvoice);
            sales.setStock(selectedStock);
            sales.setCustomer(selectedCustomer);
            sales.setQuantity(quantity);
            sales.setPrice(price);

            sales.setBrand(selectedStock.getCategory().getBrand());
            sales.setType(selectedStock.getCategory().getProductType());

            BigDecimal subTotal = price.multiply(BigDecimal.valueOf(quantity));
            sales.setSubTotal(subTotal);

            BigDecimal total = subTotal;
            sales.setPriceTotal(total);
            sales.setTotalPrice(subTotal);

            pendingSales.add(sales);
            updateSellTotalPrice();
            resetSellButton(event);
        } catch (Exception e) {
            ShowAlert.showError("Gagal menambahkan penjualan: " + e.getMessage());
            System.out.println(e.getMessage());
        }
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

                this.finalInvoiceNumber = generateFinalInvoiceNumber("BLI");

                Session session = HibernateUtil.getSessionFactory().openSession();
                Transaction transaction = null;

                try {
                    transaction = session.beginTransaction();

                    for (Purchasing purchasing : purchasingList) {
                        purchasing.setInvoiceNumber(finalInvoiceNumber);

                        Stock stock = session.get(Stock.class, purchasing.getStock().getId());
                        purchasing.setStock(stock);
                        purchasing.setSupplier(session.get(Supplier.class, purchasing.getSupplier().getId()));

                        purchasing.setBrand(stock.getCategory().getBrand());
                        purchasing.setType(stock.getCategory().getProductType());

                        session.persist(purchasing);

                        stock.setQuantity(stock.getQuantity() + purchasing.getQuantity());
                        session.merge(stock);
                    }

                    transaction.commit();

                    Platform.runLater(() -> {
                        buyTable.getItems().clear();
                        currentPendingBuyInvoice = generatePendingInvoiceNumber("BLI");
                        buyIdValue = buyId++;
                        buyInvoiceNumber.setText(String.format("TRX-%05d", buyIdValue));
                        updateBuyTotalPrice();

                        try {
                            printJasperBuyList();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        ShowAlert.showSuccess("Pembelian berhasil dikonfirmasi");
                    });

                } catch (Exception e) {
                    if (transaction != null) {
                        transaction.rollback();
                    }
                    success = false;
                    e.printStackTrace();
                    Platform.runLater(() -> ShowAlert.showError("Gagal mengkonfirmasi pembelian: " + e.getMessage()));
                } finally {
                    session.close();
                }

                if (!success) {
                    return;
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

                this.finalInvoiceNumber = generateFinalInvoiceNumber("JUL");

                Session session = HibernateUtil.getSessionFactory().openSession();
                Transaction transaction = null;

                try {
                    transaction = session.beginTransaction();

                    for (Sales sale : salesList) {
                        sale.setInvoiceNumber(finalInvoiceNumber);

                        Stock stock = session.get(Stock.class, sale.getStock().getId());
                        sale.setStock(stock);
                        sale.setCustomer(session.get(Customer.class, sale.getCustomer().getId()));

                        sale.setBrand(stock.getCategory().getBrand());
                        sale.setType(stock.getCategory().getProductType());

                        if (stock.getQuantity() < sale.getQuantity()) {
                            throw new Exception("Stok tidak mencukupi untuk " + stock.getCategory().getBrand()
                                    + " " + stock.getCategory().getProductType());
                        }

                        session.persist(sale);

                        stock.setQuantity(stock.getQuantity() - sale.getQuantity());
                        session.merge(stock);
                    }

                    transaction.commit();

                    Platform.runLater(() -> {
                        sellTable.getItems().clear();
                        currentPendingSellInvoice = generatePendingInvoiceNumber("JUL");
                        sellIdValue = sellId++;
                        sellInvoiceNumber.setText(String.format("TRX-%05d", sellIdValue));
                        updateSellTotalPrice();

                        try {
                            printJasperSellList();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        ShowAlert.showSuccess("Penjualan berhasil dikonfirmasi");
                    });

                } catch (Exception e) {
                    if (transaction != null) {
                        transaction.rollback();
                    }
                    success = false;
                    e.printStackTrace();
                    Platform.runLater(() -> ShowAlert.showError("Gagal mengkonfirmasi penjualan: " + e.getMessage()));
                } finally {
                    session.close();
                }

                if (!success) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                ShowAlert.showError("Gagal mengkonfirmasi penjualan: " + e.getMessage());
            }
        }
    }

    @FXML
    private void editBuyButton(ActionEvent event) {
        Purchasing selectedPurchase = buyTable.getSelectionModel().getSelectedItem();
        if (selectedPurchase == null) {
            ShowAlert.showWarning("Silakan pilih pembelian yang akan diubah");
            return;
        }

        GenericEditPopup.create(Purchasing.class)
                .withTitle("Ubah Pembelian")
                .forItem(selectedPurchase)
                .addField("Tanggal", new DatePicker(selectedPurchase.getPurchaseDate()), true)
                .addField("No Faktur", new TextField(selectedPurchase.getInvoiceNumber()), true)
                .addField("Barang", createStockComboBox(selectedPurchase.getStock()), true)
                .addField("Pemasok", createSupplierComboBox(selectedPurchase.getSupplier()), true)
                .addField("Jumlah", new TextField(String.valueOf(selectedPurchase.getQuantity())), false)
                .addField("Harga", createFormattedTextField(selectedPurchase.getPrice()), true)
                .onSave((item, fields) -> {
                    try {
                        int newQuantity = Integer.parseInt(((TextField) fields.get(4)).getText());
                        if (newQuantity <= 0) {
                            ShowAlert.showValidationError("Jumlah harus lebih besar dari 0");
                            return;
                        }

                        item.setQuantity(newQuantity);
                        BigDecimal subTotal = item.getPrice().multiply(BigDecimal.valueOf(newQuantity));
                        item.setSubTotal(subTotal);
                        item.setPriceTotal(subTotal);

                        pendingPurchases.remove(selectedPurchase);
                        pendingPurchases.add(item);
                        updateBuyTotalPrice();
                        buyTable.refresh();

                        ShowAlert.showSuccess("Pembelian berhasil diubah");
                    } catch (NumberFormatException e) {
                        ShowAlert.showValidationError("Format jumlah tidak valid");
                    }
                })
                .show();
    }

    @FXML
    private void editSellButton(ActionEvent event) {
        Sales selectedSale = sellTable.getSelectionModel().getSelectedItem();
        if (selectedSale == null) {
            ShowAlert.showWarning("Silakan pilih penjualan yang akan diubah");
            return;
        }

        GenericEditPopup.create(Sales.class)
                .withTitle("Ubah Penjualan")
                .forItem(selectedSale)
                .addField("Tanggal", new DatePicker(selectedSale.getSaleDate()), true)
                .addField("No Faktur", new TextField(selectedSale.getInvoiceNumber()), true)
                .addField("Barang", createStockComboBox(selectedSale.getStock()), true)
                .addField("Pelanggan", createCustomerComboBox(selectedSale.getCustomer()), true)
                .addField("Jumlah", new TextField(String.valueOf(selectedSale.getQuantity())), false)
                .addField("Harga", createFormattedTextField(selectedSale.getPrice()), true)
                .onSave((item, fields) -> {
                    try {
                        int newQuantity = Integer.parseInt(((TextField) fields.get(4)).getText());
                        if (newQuantity <= 0) {
                            ShowAlert.showValidationError("Jumlah harus lebih besar dari 0");
                            return;
                        }

                        Stock stock = selectedSale.getStock();
                        if (newQuantity > stock.getQuantity() + selectedSale.getQuantity()) {
                            ShowAlert.showValidationError("Stok tidak mencukupi. Stok tersedia: " +
                                    (stock.getQuantity() + selectedSale.getQuantity()));
                            return;
                        }

                        item.setQuantity(newQuantity);
                        BigDecimal subTotal = item.getPrice().multiply(BigDecimal.valueOf(newQuantity));
                        item.setSubTotal(subTotal);
                        item.setPriceTotal(subTotal);

                        pendingSales.remove(selectedSale);
                        pendingSales.add(item);
                        updateSellTotalPrice();
                        sellTable.refresh();

                        ShowAlert.showSuccess("Penjualan berhasil diubah");
                    } catch (NumberFormatException e) {
                        ShowAlert.showValidationError("Format jumlah tidak valid");
                    }
                })
                .show();
    }

    private JFXComboBox<Stock> createStockComboBox(Stock selectedStock) {
        JFXComboBox<Stock> comboBox = new JFXComboBox<>(buyStockIDDropdown.getItems());
        comboBox.setValue(selectedStock);
        comboBox.setConverter(buyStockIDDropdown.getConverter());
        return comboBox;
    }

    private JFXComboBox<Supplier> createSupplierComboBox(Supplier selectedSupplier) {
        JFXComboBox<Supplier> comboBox = new JFXComboBox<>(supplierIDDropDown.getItems());
        comboBox.setValue(selectedSupplier);
        comboBox.setConverter(supplierIDDropDown.getConverter());
        return comboBox;
    }

    private JFXComboBox<Customer> createCustomerComboBox(Customer selectedCustomer) {
        JFXComboBox<Customer> comboBox = new JFXComboBox<>(customerIDDropDown.getItems());
        comboBox.setValue(selectedCustomer);
        comboBox.setConverter(customerIDDropDown.getConverter());
        return comboBox;
    }

    private TextField createFormattedTextField(BigDecimal value) {
        TextField field = new TextField(NumberFormatter.formatValue(value));
        NumberFormatter.applyNumberFormat(field);
        return field;
    }

    @FXML
    private void removeBuyButton(ActionEvent event) {
        Purchasing selectedPurchase = buyTable.getSelectionModel().getSelectedItem();
        if (selectedPurchase == null) {
            ShowAlert.showWarning("Silahkan pilih pembelian yang akan dihapus");
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
            ShowAlert.showWarning("Silahkan pilih penjualan yang akan dihapus");
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
                buyInvoiceNumber, buyStockIDDropdown, supplierIDDropDown, buyTotalField, buyPriceField,
                buyBrandField, buyTypeField, supplierNameField);
    }

    @FXML
    private void resetSellButton(ActionEvent event) {
        ClearFields.clearFields(
                sellInvoiceNumber, sellStockIDDropdown, customerIDDropDown, sellTotalField, sellPriceField,
                sellBrandField, sellTypeField, customerNameField);
    }

    private boolean validateBuyFields() {
        if (buyDate.getValue() == null) {
            ShowAlert.showValidationError("Tanggal pembelian harus diisi");
            return false;
        }
        if (buyStockIDDropdown.getValue() == null) {
            ShowAlert.showValidationError("Barang harus dipilih");
            return false;
        }
        if (supplierIDDropDown.getValue() == null) {
            ShowAlert.showValidationError("Pemasok harus dipilih");
            return false;
        }
        if (buyTotalField.getText().isEmpty()) {
            ShowAlert.showValidationError("Jumlah harus diisi");
            return false;
        }
        if (buyPriceField.getText().isEmpty()) {
            ShowAlert.showValidationError("Harga harus diisi");
            return false;
        }
        return true;
    }

    private boolean validateSellFields() {
        if (sellDate.getValue() == null) {
            ShowAlert.showValidationError("Tanggal penjualan harus diisi");
            return false;
        }
        if (sellStockIDDropdown.getValue() == null) {
            ShowAlert.showValidationError("Barang harus dipilih");
            return false;
        }
        if (customerIDDropDown.getValue() == null) {
            ShowAlert.showValidationError("Pelanggan harus dipilih");
            return false;
        }
        if (sellTotalField.getText().isEmpty()) {
            ShowAlert.showValidationError("Jumlah harus diisi");
            return false;
        }
        if (sellPriceField.getText().isEmpty()) {
            ShowAlert.showValidationError("Harga harus diisi");
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
                    finalInvoiceNumber);

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
                    finalInvoiceNumber);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String generatePendingInvoiceNumber(String prefix) {
        LocalDate currentDate = LocalDate.now();
        return String.format("%s-%s-%08d",
                prefix,
                currentDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE),
                System.nanoTime() % 100000000);
    }

    private String generateFinalInvoiceNumber(String prefix) {
        LocalDate currentDate = LocalDate.now();
        long timestamp = System.currentTimeMillis();
        String randomSuffix = String.format("%04d", (int) (Math.random() * 10000));
        return String.format("%s-%s-%d-%s",
                prefix,
                currentDate.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE),
                timestamp % 1000000,
                randomSuffix);
    }
}
