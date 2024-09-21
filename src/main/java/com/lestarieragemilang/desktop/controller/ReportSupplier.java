package com.lestarieragemilang.desktop.controller;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

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
    try {
      String path = "/com/lestarieragemilang/app/desktop/jasper/supplier-list.jasper";
      URL url = ReportSupplier.class.getResource(path).toURI().toURL();
      JasperLoader loader = new JasperLoader();
      loader.showJasperReportSupplier(
          url,
          supplierSearchField.getText(),
          supplierSearchField.getText(),
          supplierSearchField.getText(),
          supplierSearchField.getText());
    } catch (IOException | URISyntaxException e) {
      LOGGER.log(Level.SEVERE, "Error loading report", e);
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