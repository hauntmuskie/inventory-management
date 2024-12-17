package com.lestarieragemilang.desktop.controller.report;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.lestarieragemilang.desktop.model.Supplier;
import com.lestarieragemilang.desktop.utils.TableUtils;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;

public class ReportSupplier {
  private static final Logger LOGGER = Logger.getLogger(ReportSupplier.class.getName());

  @FXML
  private TableColumn<Supplier, String> supplierAddressCol;

  @FXML
  private TableColumn<Supplier, String> supplierContactCol;

  @FXML
  private TableColumn<Supplier, String> supplierEmailCol;

  @FXML
  private TableColumn<Supplier, Integer> supplierIDCol;

  @FXML
  private TableColumn<Supplier, String> supplierNameCol;

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
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText(null);
      alert.setContentText("Could not find report template");
      alert.showAndWait();
      return;
    }

    Supplier selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
    if (selectedSupplier == null) {
      Alert alert = new Alert(AlertType.INFORMATION);
      alert.setTitle("Information");
      alert.setHeaderText(null);
      alert.setContentText("Please select a supplier to print");
      alert.showAndWait();
      return;
    }

    try {
      JasperLoader loader = new JasperLoader();
      loader.showJasperReportSupplier(
          url,
          selectedSupplier.getSupplierName(),
          selectedSupplier.getContact(),
          selectedSupplier.getAddress(),
          selectedSupplier.getEmail(),
          event
      );
    } catch (Exception e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText(null);
      alert.setContentText("Error generating report: " + e.getMessage());
      alert.showAndWait();
      LOGGER.log(Level.SEVERE, "Error generating report", e);
    }
  }

  @FXML
  void supplierSearch() {
    String searchText = supplierSearchField.getText().toLowerCase();
    filteredData.setPredicate(supplier ->
        searchText.isEmpty() ||
        supplier.getSupplierName().toLowerCase().contains(searchText) ||
        supplier.getAddress().toLowerCase().contains(searchText) ||
        supplier.getContact().toLowerCase().contains(searchText) ||
        supplier.getEmail().toLowerCase().contains(searchText)
    );
  }

  @FXML
  void initialize() throws SQLException {
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
        TableUtils.createColumn("Supplier ID", "supplierID"),
        TableUtils.createColumn("Name", "supplierName"),
        TableUtils.createColumn("Address", "supplierAddress"),
        TableUtils.createColumn("Contact", "supplierContact"),
        TableUtils.createColumn("Email", "supplierEmail")
    );

    TableUtils.populateTable(supplierTable, columns, suppliers);
  }

  private void setupSearch() {
    filteredData = new FilteredList<>(supplierTable.getItems(), p -> true);
    supplierTable.setItems(filteredData);
    supplierSearchField.textProperty().addListener((observable, oldValue, newValue) -> supplierSearch());
  }
}