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
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import org.hibernate.Session;
import org.hibernate.query.Query;

import com.lestarieragemilang.desktop.model.Purchasing;
import com.lestarieragemilang.desktop.utils.TableUtils;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;

public class ReportPurchasing {

  @FXML
  private DatePicker BuyListDateFirstField;

  @FXML
  private DatePicker BuyListDateSecondField;

  @FXML
  private TextField BuyListSearchField;

  @FXML
  private TableColumn<Purchasing, LocalDate> buyDateCol;

  @FXML
  private TableColumn<Purchasing, String> buyInvoiceCol;

  @FXML
  private TableColumn<Purchasing, String> buySupplierCol;

  @FXML
  private TableColumn<Purchasing, Integer> buyQuantityCol;

  @FXML
  private TableColumn<Purchasing, BigDecimal> buyPriceCol;

  @FXML
  private TableColumn<Purchasing, BigDecimal> buySubTotalCol;

  @FXML
  private TableColumn<Purchasing, BigDecimal> buyTotalCol;

  @FXML
  private TableView<Purchasing> buyTable;

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
    if (url == null) {
      path = "/com/lestarieragemilang/desktop/jasper/purchasing-list.jasper";
      url = getClass().getResource(path);
    }

    if (url == null) {
      showError("Could not find report template");
      return;
    }

    try {
      JasperLoader loader = new JasperLoader();
      LocalDate firstLocalDate = BuyListDateFirstField.getValue();
      LocalDate secondLocalDate = BuyListDateSecondField.getValue();
      String searchText = BuyListSearchField.getText();

      Date firstDate = firstLocalDate != null ? convertToDate(firstLocalDate) : null;
      Date secondDate = secondLocalDate != null ? convertToDate(secondLocalDate.plusDays(1)) : null;

      // Validate date range
      if (firstLocalDate != null && secondLocalDate != null && firstLocalDate.isAfter(secondLocalDate)) {
        showError("Start date must be before or equal to end date");
        return;
      }

      loader.showJasperReportBuyList(
          url,
          searchText != null && !searchText.isEmpty() ? "%" + searchText + "%" : "%",
          firstDate,
          secondDate,
          event
      );
    } catch (Exception e) {
      showError("Error generating report: " + e.getMessage());
    }
  }

  private Date convertToDate(LocalDate localDate) {
    return localDate != null ? 
        Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
  }

  private void showError(String message) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
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
      Query<Purchasing> query = session.createQuery("FROM Purchasing", Purchasing.class);
      return query.list();
    } catch (Exception e) {
      e.printStackTrace();
      return FXCollections.emptyObservableList();
    }
  }

  private void setupTable(List<Purchasing> purchases) {
    List<TableColumn<Purchasing, ?>> columns = List.of(
        TableUtils.createColumn("Date", "purchaseDate"),
        TableUtils.createColumn("Invoice", "invoiceNumber"),
        TableUtils.createColumn("Supplier", "supplier.supplierName"),
        TableUtils.createColumn("Stock", "stock.category.brand"),
        TableUtils.createColumn("Quantity", "quantity"),
        TableUtils.createFormattedColumn("Price", "price"),
        TableUtils.createFormattedColumn("Sub Total", "subTotal"),
        TableUtils.createFormattedColumn("Total", "priceTotal")
    );

    TableUtils.populateTable(buyTable, columns, purchases);
  }

  private void setupSearch() {
    filteredData = new FilteredList<>(buyTable.getItems(), p -> true);
    buyTable.setItems(filteredData);
    BuyListSearchField.textProperty().addListener((observable, oldValue, newValue) -> purchaseSearch());
  }

  private void setupDateSearchMutualExclusion() {
    // Disable search when dates are selected
    BuyListDateFirstField.valueProperty().addListener((obs, old, newValue) -> {
        BuyListSearchField.setDisable(newValue != null);
        if (newValue != null) {
            BuyListSearchField.clear();
        }
    });

    BuyListDateSecondField.valueProperty().addListener((obs, old, newValue) -> {
        BuyListSearchField.setDisable(newValue != null);
        if (newValue != null) {
            BuyListSearchField.clear();
        }
    });

    // Disable dates when search is used
    BuyListSearchField.textProperty().addListener((obs, old, newValue) -> {
        boolean hasText = !newValue.isEmpty();
        BuyListDateFirstField.setDisable(hasText);
        BuyListDateSecondField.setDisable(hasText);
        if (hasText) {
            BuyListDateFirstField.setValue(null);
            BuyListDateSecondField.setValue(null);
        }
    });
  }

  @SuppressWarnings("unused")
  private void addListeners() {
    BuyListSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!newValue.isEmpty()) {
        clearDateFields();
      }
    });

    BuyListDateFirstField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        BuyListSearchField.clear();
      }
    });

    BuyListDateSecondField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        BuyListSearchField.clear();
      }
    });
  }

  private void clearDateFields() {
    BuyListDateFirstField.setValue(null);
    BuyListDateSecondField.setValue(null);
  }
}