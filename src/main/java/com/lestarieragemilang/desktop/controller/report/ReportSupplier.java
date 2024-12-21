package com.lestarieragemilang.desktop.controller.report;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.lestarieragemilang.desktop.model.Supplier;
import com.lestarieragemilang.desktop.utils.TableUtils;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;
import com.lestarieragemilang.desktop.utils.ShowAlert;

public class ReportSupplier {
  private static final Logger LOGGER = Logger.getLogger(ReportSupplier.class.getName());

  @FXML
  private TableView<Supplier> supplierTable;

  @FXML
  private TextField supplierSearchField;

  private FilteredList<Supplier> filteredData;

  @FXML
  void printJasperSupplier(MouseEvent event) {
    String path = "/jasper/supplier-list.jasper";
    URL url = getClass().getResource(path);
    if (url == null) {
      path = "/com/lestarieragemilang/desktop/jasper/supplier-list.jasper";
      url = getClass().getResource(path);
    }

    if (url == null) {
      ShowAlert.showAlert(AlertType.ERROR, 
          "Error", 
          "Kesalahan Template", 
          "Template laporan tidak ditemukan");
      return;
    }

    try {
      JasperLoader loader = new JasperLoader();
      Supplier supplier = supplierTable.getSelectionModel().getSelectedItem();
      if (supplier != null) {
        loader.showJasperReportSupplier(
            url,
            supplier.getSupplierId(),
            supplier.getSupplierName(),
            supplier.getContact(),
            supplier.getAddress(), 
            supplier.getEmail(),
            event
        );
      } else {
        loader.showJasperReportSupplier(
            url,
            "%", "%", "%", "%", "%", event
        );
      }
    } catch (Exception e) {
      ShowAlert.showAlert(AlertType.ERROR, 
          "Error", 
          "Kesalahan Laporan", 
          "Terjadi kesalahan saat membuat laporan:", 
          e.getMessage());
      LOGGER.log(Level.SEVERE, "Error generating report", e);
    }
  }

  @FXML
  void supplierSearch() {
    String searchText = supplierSearchField.getText().toLowerCase();
    filteredData.setPredicate(supplier -> {
      if (searchText == null || searchText.isEmpty()) {
        return true;
      }
      return supplier.getSupplierId().toLowerCase().contains(searchText) ||
          supplier.getSupplierName().toLowerCase().contains(searchText) ||
          supplier.getContact().toLowerCase().contains(searchText) ||
          supplier.getAddress().toLowerCase().contains(searchText) ||
          supplier.getEmail().toLowerCase().contains(searchText);
    });
  }

  @FXML
  void initialize() {
    List<Supplier> suppliers = fetchSuppliersFromDatabase();
    setupTable(suppliers);
    setupSearch();
  }

  private List<Supplier> fetchSuppliersFromDatabase() {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      Query<Supplier> query = session.createQuery("FROM Supplier", Supplier.class);
      return query.list();
    } catch (Exception e) {
      e.printStackTrace();
      return FXCollections.emptyObservableList();
    }
  }

  private void setupTable(List<Supplier> suppliers) {
    List<TableColumn<Supplier, ?>> columns = List.of(
        TableUtils.createColumn("Supplier ID", "supplierId"),
        TableUtils.createColumn("Name", "supplierName"),
        TableUtils.createColumn("Contact", "contact"),
        TableUtils.createColumn("Address", "address"),
        TableUtils.createColumn("Email", "email")
    );

    TableUtils.populateTable(supplierTable, columns, suppliers);
  }

  private void setupSearch() {
    filteredData = new FilteredList<>(supplierTable.getItems(), p -> true);
    supplierTable.setItems(filteredData);
    supplierSearchField.textProperty().addListener((observable, oldValue, newValue) -> supplierSearch());
  }
}