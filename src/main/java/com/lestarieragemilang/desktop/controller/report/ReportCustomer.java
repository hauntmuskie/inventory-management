package com.lestarieragemilang.desktop.controller.report;

import com.lestarieragemilang.desktop.model.Customer;
import com.lestarieragemilang.desktop.utils.TableUtils;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.net.URL;
import java.util.List;

public class ReportCustomer {

  @FXML
  private TableView<Customer> customerTable;

  @FXML
  private TextField customerSearchField;

  private FilteredList<Customer> filteredData;

  @FXML
  void printJasperCustomer(MouseEvent event) {
    String path = "/jasper/customer-list.jasper";
    URL url = getClass().getResource(path);
    if (url == null) {
      path = "/com/lestarieragemilang/desktop/jasper/customer-list.jasper";
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

    try {
      JasperLoader loader = new JasperLoader();
      String searchText = customerSearchField.getText();

      if (searchText != null && !searchText.isEmpty()) {
        loader.showJasperReportCustomer(
            url,
            "%" + searchText + "%",
            "%" + searchText + "%",
            "%" + searchText + "%",
            "%" + searchText + "%",
            event
        );
      } else {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer != null) {
          loader.showJasperReportCustomer(
              url,
              selectedCustomer.getCustomerName(),
              selectedCustomer.getContact(),
              selectedCustomer.getAddress(),
              selectedCustomer.getEmail(),
              event
          );
        } else {
          loader.showJasperReportCustomer(
              url,
              "%", "%", "%", "%", event
          );
        }
      }
    } catch (Exception e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText(null);
      alert.setContentText("Error generating report: " + e.getMessage());
      alert.showAndWait();
    }
  }

  @FXML
  void customerSearch() {
    String searchText = customerSearchField.getText().toLowerCase();
    filteredData.setPredicate(customer -> {
      if (searchText == null || searchText.isEmpty()) {
        return true;
      }
      return customer.getCustomerId().toLowerCase().contains(searchText) ||
          customer.getCustomerName().toLowerCase().contains(searchText) ||
          customer.getContact().toLowerCase().contains(searchText) ||
          customer.getAddress().toLowerCase().contains(searchText) ||
          customer.getEmail().toLowerCase().contains(searchText);
    });
  }

  @FXML
  void initialize() {
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
        TableUtils.createColumn("Customer ID", "customerId"),
        TableUtils.createColumn("Name", "customerName"),
        TableUtils.createColumn("Contact", "contact"),
        TableUtils.createColumn("Address", "address"),
        TableUtils.createColumn("Email", "email")
    );

    TableUtils.populateTable(customerTable, columns, customers);
  }

  private void setupSearch() {
    filteredData = new FilteredList<>(customerTable.getItems(), p -> true);
    customerTable.setItems(filteredData);
    customerSearchField.textProperty().addListener((observable, oldValue, newValue) -> customerSearch());
  }
}