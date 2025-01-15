package com.lestarieragemilang.desktop.controller.report;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.hibernate.Session;
import com.lestarieragemilang.desktop.model.Purchasing;
import com.lestarieragemilang.desktop.utils.TableUtils;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;
import com.lestarieragemilang.desktop.utils.ShowAlert;

public class ReportPurchasing {
    @FXML private DatePicker BuyListDateFirstField, BuyListDateSecondField;
    @FXML private TextField BuyListSearchField;
    @FXML private TableView<Purchasing> buyTable;
    @FXML private TableColumn<Purchasing, LocalDate> buyDateCol;
    @FXML private TableColumn<Purchasing, String> buyInvoiceCol, buySupplierCol;
    @FXML private TableColumn<Purchasing, Integer> buyQuantityCol;
    @FXML private TableColumn<Purchasing, BigDecimal> buyPriceCol, buySubTotalCol, buyTotalCol;

    private FilteredList<Purchasing> filteredData;

    @FXML
    void purchaseSearch() {
        String searchText = BuyListSearchField.getText().toLowerCase();
        filteredData.setPredicate(purchase -> searchText.isEmpty() || 
            String.valueOf(purchase.getInvoiceNumber()).toLowerCase().contains(searchText));
    }

    @FXML
    void printJasperBuyList(MouseEvent event) {
        String path = "/jasper/purchasing-list.jasper";
        URL url = getClass().getResource(path);
        if (url == null) url = getClass().getResource("/com/lestarieragemilang/desktop/jasper/purchasing-list.jasper");
        if (url == null) {
            ShowAlert.showError("Template laporan tidak ditemukan");
            return;
        }

        try {
            JasperLoader loader = new JasperLoader();
            String searchText = BuyListSearchField.getText();
            LocalDate firstLocalDate = BuyListDateFirstField.getValue(), secondLocalDate = BuyListDateSecondField.getValue();

            if (searchText != null && !searchText.isEmpty()) 
                loader.showJasperReportBuyList(url, "%" + searchText + "%", null, null, event);
            else if (firstLocalDate != null && secondLocalDate != null)
                loader.showJasperReportBuyList(url, null, convertToDate(firstLocalDate), convertToDate(secondLocalDate), event);
            else 
                loader.showJasperReportBuyList(url, "%", null, null, event);
        } catch (Exception e) {
            ShowAlert.showError("Terjadi kesalahan saat membuat laporan: " + e.getMessage());
        }
    }

    private Date convertToDate(LocalDate localDate) {
        return localDate != null ? Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
    }

    @FXML
    void initialize() throws SQLException {
        List<Purchasing> purchases = fetchPurchasesFromDatabase();
        setupTable(purchases);
        setupSearch();
        setupDateSearchMutualExclusion();
    }

    private List<Purchasing> fetchPurchasesFromDatabase() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Purchasing", Purchasing.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.emptyObservableList();
        }
    }

    private void setupTable(List<Purchasing> purchases) {
        List<TableColumn<Purchasing, ?>> columns = List.of(
            TableUtils.createColumn("Tanggal", "purchaseDate"),
            TableUtils.createColumn("Nomor Faktur", "invoiceNumber"),
            TableUtils.createColumn("Pemasok", "supplier.supplierName"),
            TableUtils.createColumn("Stok", "stock.category.brand"),
            TableUtils.createColumn("Jumlah", "quantity"),
            TableUtils.createFormattedColumn("Harga", "price"),
            TableUtils.createFormattedColumn("Sub Total", "subTotal"),
            TableUtils.createFormattedColumn("Total", "priceTotal"));
        TableUtils.populateTable(buyTable, columns, purchases);
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(buyTable.getItems(), _ -> true);
        buyTable.setItems(filteredData);
        BuyListSearchField.textProperty().addListener((_, _, _) -> purchaseSearch());
    }

    private void setupDateSearchMutualExclusion() {
        BuyListDateFirstField.valueProperty().addListener((_, _, newValue) -> {
            BuyListSearchField.setDisable(newValue != null);
            if (newValue != null) BuyListSearchField.clear();
        });

        BuyListDateSecondField.valueProperty().addListener((_, _, newValue) -> {
            BuyListSearchField.setDisable(newValue != null);
            if (newValue != null) BuyListSearchField.clear();
        });

        BuyListSearchField.textProperty().addListener((_, _, newValue) -> {
            boolean hasText = !newValue.isEmpty();
            BuyListDateFirstField.setDisable(hasText);
            BuyListDateSecondField.setDisable(hasText);
            if (hasText) {
                BuyListDateFirstField.setValue(null);
                BuyListDateSecondField.setValue(null);
            }
        });
    }
}