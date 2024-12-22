package com.lestarieragemilang.desktop.controller.report;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Alert.AlertType;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.lestarieragemilang.desktop.model.Sales;
import com.lestarieragemilang.desktop.utils.TableUtils;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;
import com.lestarieragemilang.desktop.utils.ShowAlert;

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
  private TableColumn<Sales, String> sellInvoiceCol;

  @FXML
  private TableColumn<Sales, String> sellCustomerCol;

  @FXML
  private TableColumn<Sales, Integer> sellQuantityCol;

  @FXML
  private TableColumn<Sales, BigDecimal> sellPriceCol;

  @FXML
  private TableColumn<Sales, BigDecimal> sellSubTotalCol;

  @FXML
  private TableView<Sales> sellTable;

  @FXML
  private TableColumn<Sales, BigDecimal> sellTotalCol;

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
      ShowAlert.showAlert(AlertType.ERROR, 
          "Error", 
          "Kesalahan Template", 
          "Template laporan tidak ditemukan");
      return;
    }

    try {
      JasperLoader loader = new JasperLoader();
      String searchText = SellListSearchField.getText();
      LocalDate firstLocalDate = SellListDateFirstField.getValue();
      LocalDate secondLocalDate = SellListDateSecondField.getValue();

      if (searchText != null && !searchText.isEmpty()) {
        loader.showJasperReportSellList(
            url,
            "%" + searchText + "%",
            null,
            null,
            event
        );
      } else if (firstLocalDate != null && secondLocalDate != null) {
        Date firstDate = convertToDate(firstLocalDate);
        Date secondDate = convertToDate(secondLocalDate.plusDays(1));
        loader.showJasperReportSellList(
            url,
            "%",
            firstDate,
            secondDate,
            event
        );
      } else {
        loader.showJasperReportSellList(
            url,
            "%",
            null,
            null,
            event
        );
      }
    } catch (Exception e) {
      ShowAlert.showAlert(AlertType.ERROR, 
          "Error", 
          "Kesalahan Laporan", 
          "Terjadi kesalahan saat membuat laporan:", 
          e.getMessage());
    }
  }

  private Date convertToDate(LocalDate localDate) {
    return localDate != null ? 
        Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
  }

  private void showError(String message) {
    ShowAlert.showError(message);
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
    setupDateSearchMutualExclusion();
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
        TableUtils.createColumn("Date", "saleDate"),
        TableUtils.createColumn("Invoice", "invoiceNumber"),
        TableUtils.createColumn("Customer", "customer.customerName"),
        TableUtils.createColumn("Stock", "stock.category.brand"),
        TableUtils.createColumn("Quantity", "quantity"),
        TableUtils.createFormattedColumn("Price", "price"),
        TableUtils.createFormattedColumn("Sub Total", "subTotal"),
        TableUtils.createFormattedColumn("Total", "priceTotal")
    );

    TableUtils.populateTable(sellTable, columns, sales);
  }

  private void setupSearch() {
    filteredData = new FilteredList<>(sellTable.getItems(), p -> true);
    sellTable.setItems(filteredData);
    SellListSearchField.textProperty().addListener((observable, oldValue, newValue) -> sellSearch());
  }

  private void setupDateSearchMutualExclusion() {
    // Disable search when dates are selected
    SellListDateFirstField.valueProperty().addListener((obs, old, newValue) -> {
        SellListSearchField.setDisable(newValue != null);
        if (newValue != null) {
            SellListSearchField.clear();
        }
    });

    SellListDateSecondField.valueProperty().addListener((obs, old, newValue) -> {
        SellListSearchField.setDisable(newValue != null);
        if (newValue != null) {
            SellListSearchField.clear();
        }
    });

    // Disable dates when search is used
    SellListSearchField.textProperty().addListener((obs, old, newValue) -> {
        boolean hasText = !newValue.isEmpty();
        SellListDateFirstField.setDisable(hasText);
        SellListDateSecondField.setDisable(hasText);
        if (hasText) {
            SellListDateFirstField.setValue(null);
            SellListDateSecondField.setValue(null);
        }
    });
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