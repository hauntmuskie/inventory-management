package com.lestarieragemilang.desktop.controller;

import com.lestarieragemilang.desktop.model.Customer;
import com.lestarieragemilang.desktop.utils.TableUtils;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class ReportCustomer {

  @FXML
  private TableColumn<Customer, String> customerAddressCol;

  @FXML
  private TableColumn<Customer, String> customerContactCol;

  @FXML
  private TableColumn<Customer, String> customerEmailCol;

  @FXML
  private TableColumn<Customer, Integer> customerIDCol;

  @FXML
  private TableColumn<Customer, String> customerNameCol;

  @FXML
  private TableView<Customer> customerTable;

  @FXML
  private TextField customerSearchField;

  private FilteredList<Customer> filteredData;

  @FXML
  void printJasperCustomer(MouseEvent event) throws IOException, URISyntaxException {
    String path = "/com/lestarieragemilang/app/desktop/jasper/customer-list.jasper";
    URL url = getClass().getResource(path).toURI().toURL();
    try {
      JasperLoader loader = new JasperLoader();
      String searchText = customerSearchField.getText();
      loader.showJasperReportCustomer(url, searchText, searchText, searchText, searchText);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
  void customerSearch() {
    String searchText = customerSearchField.getText().toLowerCase();
    filteredData.setPredicate(customer -> searchText.isEmpty() ||
        customer.getCustomerName().toLowerCase().contains(searchText) ||
        customer.getAddress().toLowerCase().contains(searchText) ||
        customer.getContact().toLowerCase().contains(searchText) ||
        customer.getEmail().toLowerCase().contains(searchText));
  }

  @FXML
  void initialize() throws SQLException {
    List<Customer> customers = fetchCustomersFromDatabase();
    setupTable(customers);
    setupSearch();
  }

  private List<Customer> fetchCustomersFromDatabase() {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      Query<Customer> query = session.createQuery("FROM Customer", Customer.class);
      return query.list();
    } catch (Exception e) {
      e.printStackTrace();
      return FXCollections.emptyObservableList();
    }
  }

  private void setupTable(List<Customer> customers) {
    List<TableColumn<Customer, ?>> columns = List.of(
        TableUtils.createColumn("Customer ID", "customerID"),
        TableUtils.createColumn("Name", "customerName"),
        TableUtils.createColumn("Address", "customerAddress"),
        TableUtils.createColumn("Contact", "customerContact"),
        TableUtils.createColumn("Email", "customerEmail"));

    TableUtils.populateTable(customerTable, columns, customers);
  }

  private void setupSearch() {
    filteredData = new FilteredList<>(customerTable.getItems(), p -> true);
    customerTable.setItems(filteredData);
    customerSearchField.textProperty().addListener((observable, oldValue, newValue) -> customerSearch());
  }
}