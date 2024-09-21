package com.lestarieragemilang.desktop.controller;

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
  private TableColumn<Purchasing, Integer> buyInvoiceCol;

  @FXML
  private TableColumn<Purchasing, String> buyOnSupplierNameCol;

  @FXML
  private TableColumn<Purchasing, String> buyBrandCol;

  @FXML
  private TableColumn<Purchasing, String> buyTypeCol;

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
    String path = "/com/lestarieragemilang/app/desktop/jasper/purchasing-list.jasper";
    try {
      URL url = getClass().getResource(path);

      JasperLoader loader = new JasperLoader();

      LocalDate firstLocalDate = BuyListDateFirstField.getValue();
      LocalDate secondLocalDate = BuyListDateSecondField.getValue();

      Date firstDate = convertToLocalDate(firstLocalDate);
      Date secondDate = convertToLocalDate(secondLocalDate);

      loader.showJasperReportBuyList(url, BuyListSearchField.getText(), firstDate, secondDate);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private Date convertToLocalDate(LocalDate localDate) {
    return localDate != null ? Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
  }

  @FXML
  void initialize() throws SQLException {
    List<Purchasing> purchases = fetchPurchasesFromDatabase();
    setupTable(purchases);
    setupSearch();
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
        TableUtils.createColumn("Date", "buyDate"),
        TableUtils.createColumn("Invoice", "invoiceNumber"),
        TableUtils.createColumn("Supplier Name", "supplierName"),
        TableUtils.createColumn("Brand", "brand"),
        TableUtils.createColumn("Type", "type"),
        TableUtils.createColumn("Price", "price"),
        TableUtils.createColumn("Sub Total", "subTotal"),
        TableUtils.createColumn("Total", "total"));

    TableUtils.populateTable(buyTable, columns, purchases);
  }

  private void setupSearch() {
    filteredData = new FilteredList<>(buyTable.getItems(), p -> true);
    buyTable.setItems(filteredData);
    BuyListSearchField.textProperty().addListener((observable, oldValue, newValue) -> purchaseSearch());
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