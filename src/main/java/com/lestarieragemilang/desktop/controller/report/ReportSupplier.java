package com.lestarieragemilang.desktop.controller.report;

import com.lestarieragemilang.desktop.model.Supplier;
import com.lestarieragemilang.desktop.utils.*;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.hibernate.Session;
import java.net.URL;
import java.util.List;

public class ReportSupplier {
    @FXML private TableView<Supplier> supplierTable;
    @FXML private TextField supplierSearchField;
    private FilteredList<Supplier> filteredData;

    @FXML
    void printJasperSupplier(MouseEvent event) {
        String path = "/jasper/supplier-list.jasper";
        URL url = getClass().getResource(path);
        if (url == null) url = getClass().getResource("/com/lestarieragemilang/desktop/jasper/supplier-list.jasper");
        if (url == null) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Error", "Kesalahan Template", 
                "Template laporan tidak ditemukan");
            return;
        }

        try {
            JasperLoader loader = new JasperLoader();
            String searchText = supplierSearchField.getText();
            Supplier supplier = supplierTable.getSelectionModel().getSelectedItem();

            if (searchText != null && !searchText.isEmpty()) {
                loader.showJasperReportSupplier(url, searchText, searchText, searchText, 
                    searchText, searchText);
            } else if (supplier != null) {
                loader.showJasperReportSupplier(url, supplier.getSupplierId(), 
                    supplier.getSupplierName(), supplier.getContact(), 
                    supplier.getAddress(), supplier.getEmail());
            } else {
                loader.showJasperReportSupplier(url, "%", "%", "%", "%", "%");
            }
        } catch (Exception e) {
            ShowAlert.showAlert(Alert.AlertType.ERROR, "Error", "Kesalahan Laporan",
                "Terjadi kesalahan saat membuat laporan:", e.getMessage());
        }
    }

    @FXML
    void supplierSearch() {
        String searchText = supplierSearchField.getText().toLowerCase();
        filteredData.setPredicate(supplier -> {
            if (searchText == null || searchText.isEmpty()) return true;
            return supplier.getSupplierId().toLowerCase().contains(searchText) ||
                supplier.getSupplierName().toLowerCase().contains(searchText) ||
                supplier.getContact().toLowerCase().contains(searchText) ||
                supplier.getAddress().toLowerCase().contains(searchText) ||
                supplier.getEmail().toLowerCase().contains(searchText);
        });
    }

    @FXML
    void initialize() {
        setupTable(fetchSuppliersFromDatabase());
        setupSearch();
    }

    private List<Supplier> fetchSuppliersFromDatabase() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Supplier", Supplier.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.emptyObservableList();
        }
    }

    private void setupTable(List<Supplier> suppliers) {
        List<TableColumn<Supplier, ?>> columns = List.of(
            TableUtils.createColumn("Kode Pemasok", "supplierId"),
            TableUtils.createColumn("Nama", "supplierName"),
            TableUtils.createColumn("Kontak", "contact"),
            TableUtils.createColumn("Alamat", "address"),
            TableUtils.createColumn("Surel", "email")
        );
        TableUtils.populateTable(supplierTable, columns, suppliers);
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(supplierTable.getItems(), _ -> true);
        supplierTable.setItems(filteredData);
        supplierSearchField.textProperty().addListener((_, _, _) -> supplierSearch());
    }
}