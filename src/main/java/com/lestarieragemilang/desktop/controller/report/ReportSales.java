package com.lestarieragemilang.desktop.controller.report;

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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.lestarieragemilang.desktop.model.Sales;
import com.lestarieragemilang.desktop.utils.TableUtils;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;

public class ReportSales {

  @FXML
  private DatePicker SellListDateFirstField;

  @FXML
  private DatePicker SellListDateSecondField;

  @FXML
  private TextField SellListSearchField;

  @FXML
  private TableColumn<Sales, String> sellBrandCol;

  @FXML
  private TableColumn<Sales, LocalDate> sellDateCol;

  @FXML
  private TableColumn<Sales, Integer> sellInvoiceCol;

  @FXML
  private TableColumn<Sales, String> sellOnCustomerNameCol;

  @FXML
  private TableColumn<Sales, Double> sellPriceCol;

  @FXML
  private TableColumn<Sales, Double> sellSubTotalCol;

  @FXML
  private TableView<Sales> sellTable;

  @FXML
  private TableColumn<Sales, Double> sellTotalCol;

  @FXML
  private TableColumn<Sales, String> sellTypeCol;

  private FilteredList<Sales> filteredData;

  @FXML
  void printJasperSellList(MouseEvent event) {
    String path = "/jasper/sales-list.jasper";
    URL url = getClass().getResource(path);
    if (url == null) {
      path = "/com/lestarieragemilang/desktop/jasper/sales-list.jasper";
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
      LocalDate firstLocalDate = SellListDateFirstField.getValue();
      LocalDate secondLocalDate = SellListDateSecondField.getValue();

      if (firstLocalDate == null || secondLocalDate == null) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText("Please select both start and end dates");
        alert.showAndWait();
        return;
      }

      loader.showJasperReportSellList(url,
          SellListSearchField.getText(),
          Date.from(firstLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
          Date.from(secondLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
          event
      );
    } catch (Exception e) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Error");
      alert.setHeaderText(null);
      alert.setContentText("Error generating report: " + e.getMessage());
      alert.showAndWait();
    }
  }

  private Date convertToLocalDate(LocalDate localDate) {
    return localDate != null ? Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
  }

  @FXML
  void sellSearch() {
    String searchText = SellListSearchField.getText().toLowerCase();
    filteredData.setPredicate(sell ->
        searchText.isEmpty() ||
        String.valueOf(sell.getInvoiceNumber()).toLowerCase().contains(searchText)
    );
  }

  @FXML
  void initialize() throws SQLException {
    List<Sales> sales = fetchSalesFromDatabase();
    setupTable(sales);
    setupSearch();
  }

  private List<Sales> fetchSalesFromDatabase() {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      Query<Sales> query = session.createQuery("FROM Sales", Sales.class);
      return query.list();
    } catch (Exception e) {
      e.printStackTrace();
      return FXCollections.emptyObservableList();
    }
  }

  private void setupTable(List<Sales> sales) {
    List<TableColumn<Sales, ?>> columns = List.of(
        TableUtils.createColumn("Date", "sellDate"),
        TableUtils.createColumn("Invoice", "invoiceNumber"),
        TableUtils.createColumn("Customer Name", "customerName"),
        TableUtils.createColumn("Brand", "brand"),
        TableUtils.createColumn("Type", "type"),
        TableUtils.createColumn("Price", "price"),
        TableUtils.createColumn("Sub Total", "subTotal"),
        TableUtils.createColumn("Total", "total"));

    TableUtils.populateTable(sellTable, columns, sales);
  }

  private void setupSearch() {
    filteredData = new FilteredList<>(sellTable.getItems(), p -> true);
    sellTable.setItems(filteredData);
    SellListSearchField.textProperty().addListener((observable, oldValue, newValue) -> sellSearch());
  }

  @SuppressWarnings("unused")
  private void addListeners() {
    SellListSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.isEmpty()) {
        clearDateFields();
      }
    });

    SellListDateFirstField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        SellListSearchField.clear();
      }
    });

    SellListDateSecondField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        SellListSearchField.clear();
      }
    });
  }

  private void clearDateFields() {
    SellListDateFirstField.setValue(null);
    SellListDateSecondField.setValue(null);
  }
}