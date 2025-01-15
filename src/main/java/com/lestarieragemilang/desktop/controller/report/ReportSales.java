package com.lestarieragemilang.desktop.controller.report;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.math.BigDecimal;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert.AlertType;
import org.hibernate.Session;
import com.lestarieragemilang.desktop.model.Sales;
import com.lestarieragemilang.desktop.utils.*;

public class ReportSales {
    @FXML private DatePicker SellListDateFirstField, SellListDateSecondField;
    @FXML private TextField SellListSearchField;
    @FXML private TableView<Sales> sellTable;
    @FXML private TableColumn<Sales, String> sellBrandCol, sellInvoiceCol, sellCustomerCol, sellTypeCol;
    @FXML private TableColumn<Sales, LocalDate> sellDateCol;
    @FXML private TableColumn<Sales, Integer> sellQuantityCol;
    @FXML private TableColumn<Sales, BigDecimal> sellPriceCol, sellSubTotalCol, sellTotalCol;

    private FilteredList<Sales> filteredData;

    @FXML
    void printJasperSellList(MouseEvent event) {
        String path = "/jasper/sales-list.jasper";
        URL url = getClass().getResource(path);
        if (url == null) {
            url = getClass().getResource("/com/lestarieragemilang/desktop/jasper/sales-list.jasper");
            if (url == null) {
                ShowAlert.showAlert(AlertType.ERROR, "Error", "Kesalahan Template", "Template laporan tidak ditemukan");
                return;
            }
        }

        try {
            JasperLoader loader = new JasperLoader();
            String searchText = SellListSearchField.getText();
            LocalDate firstDate = SellListDateFirstField.getValue(), secondDate = SellListDateSecondField.getValue();

            if (searchText != null && !searchText.isEmpty()) {
                loader.showJasperReportSellList(url, "%" + searchText + "%", null, null, event);
            } else if (firstDate != null && secondDate != null) {
                loader.showJasperReportSellList(url, null, convertToDate(firstDate), convertToDate(secondDate), event);
            } else {
                loader.showJasperReportSellList(url, "%", null, null, event);
            }
        } catch (Exception e) {
            ShowAlert.showAlert(AlertType.ERROR, "Error", "Kesalahan Laporan", "Terjadi kesalahan saat membuat laporan:", e.getMessage());
        }
    }

    private Date convertToDate(LocalDate localDate) {
        return localDate != null ? Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
    }

    @FXML
    void sellSearch() {
        String searchText = SellListSearchField.getText().toLowerCase();
        filteredData.setPredicate(sell -> searchText.isEmpty() || 
            String.valueOf(sell.getInvoiceNumber()).toLowerCase().contains(searchText));
    }

    @FXML
    void initialize() throws SQLException {
        List<Sales> sales = fetchSalesFromDatabase();
        setupTable(sales);
        setupSearch();
        setupDateSearchMutualExclusion();
    }

    private List<Sales> fetchSalesFromDatabase() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Sales", Sales.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.emptyObservableList();
        }
    }

    private void setupTable(List<Sales> sales) {
        List<TableColumn<Sales, ?>> columns = List.of(
            TableUtils.createColumn("Tanggal", "saleDate"),
            TableUtils.createColumn("Nomor Faktur", "invoiceNumber"),
            TableUtils.createColumn("Pelanggan", "customer.customerName"),
            TableUtils.createColumn("Stok", "stock.category.brand"),
            TableUtils.createColumn("Jumlah", "quantity"),
            TableUtils.createFormattedColumn("Harga", "price"),
            TableUtils.createFormattedColumn("Sub Total", "subTotal"),
            TableUtils.createFormattedColumn("Total", "priceTotal")
        );
        TableUtils.populateTable(sellTable, columns, sales);
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(sellTable.getItems(), _ -> true);
        sellTable.setItems(filteredData);
        SellListSearchField.textProperty().addListener((_, _, _) -> sellSearch());
    }

    private void setupDateSearchMutualExclusion() {
        SellListDateFirstField.valueProperty().addListener((_, _, newValue) -> {
            SellListSearchField.setDisable(newValue != null);
            if (newValue != null) SellListSearchField.clear();
        });

        SellListDateSecondField.valueProperty().addListener((_, _, newValue) -> {
            SellListSearchField.setDisable(newValue != null);
            if (newValue != null) SellListSearchField.clear();
        });

        SellListSearchField.textProperty().addListener((_, _, newValue) -> {
            boolean hasText = !newValue.isEmpty();
            SellListDateFirstField.setDisable(hasText);
            SellListDateSecondField.setDisable(hasText);
            if (hasText) {
                SellListDateFirstField.setValue(null);
                SellListDateSecondField.setValue(null);
            }
        });
    }
}