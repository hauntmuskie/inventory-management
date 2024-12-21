package com.lestarieragemilang.desktop.controller.report;

import com.lestarieragemilang.desktop.model.Stock;
import com.lestarieragemilang.desktop.utils.TableUtils;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.JasperLoader;
import com.lestarieragemilang.desktop.utils.ShowAlert;

import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.net.URL;
import java.util.List;

public class ReportStock {

  @FXML
  private TableView<Stock> stockTable;
  @FXML
  private TextField stockSearchField;

  private FilteredList<Stock> filteredData;

  @FXML
  void printJasperStock(MouseEvent event) {
    String path = "/jasper/stock-list.jasper";
    URL url = getClass().getResource(path);
    if (url == null) {
      path = "/com/lestarieragemilang/desktop/jasper/stock-list.jasper";
      url = getClass().getResource(path);
    }
    
    if (url == null) {
      ShowAlert.showAlert(AlertType.ERROR, 
          "Error", 
          "Kesalahan Template", 
          "Template laporan tidak ditemukan");
      return;
    }

    Stock selectedStock = stockTable.getSelectionModel().getSelectedItem();
    
    try {
      JasperLoader loader = new JasperLoader();
      if (selectedStock != null) {
        loader.showJasperReportStock(url,
            selectedStock.getCategory().getBrand(),
            selectedStock.getCategory().getProductType(),
            selectedStock.getCategory().getSize(),
            selectedStock.getCategory().getWeight().toString(),
            selectedStock.getCategory().getWeightUnit(),
            selectedStock.getQuantity().toString(),
            selectedStock.getPurchasePrice().toString(),
            selectedStock.getSellingPrice().toString(),
            event
        );
      } else {
        loader.showJasperReportStock(url,
            "%", "%", "%", "%", "%", "%", "%", "%", event
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

  @FXML
  void stockSearch() {
    String searchText = stockSearchField.getText().toLowerCase();
    filteredData.setPredicate(stock -> {
      if (searchText == null || searchText.isEmpty()) {
        return true;
      }
      return stock.getStockId().toLowerCase().contains(searchText) ||
          stock.getCategory().getBrand().toLowerCase().contains(searchText) ||
          stock.getCategory().getProductType().toLowerCase().contains(searchText) ||
          stock.getCategory().getSize().toLowerCase().contains(searchText) ||
          (stock.getCategory().getWeightUnit() != null && 
           stock.getCategory().getWeightUnit().toLowerCase().contains(searchText)) ||
          stock.getQuantity().toString().toLowerCase().contains(searchText) ||
          stock.getPurchasePrice().toString().toLowerCase().contains(searchText) ||
          stock.getSellingPrice().toString().toLowerCase().contains(searchText);
    });
  }

  @FXML
  void initialize() {
    List<Stock> stocks = fetchStocksFromDatabase();
    setupTable(stocks);
    setupSearch();
  }

  private List<Stock> fetchStocksFromDatabase() {
    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
      Query<Stock> query = session.createQuery("FROM Stock", Stock.class);
      return query.list();
    } catch (Exception e) {
      e.printStackTrace();
      return FXCollections.emptyObservableList();
    }
  }

  private void setupTable(List<Stock> stocks) {
    List<TableColumn<Stock, ?>> columns = List.of(
        TableUtils.createColumn("Stock ID", "stockId"),
        TableUtils.createColumn("Brand", "category.brand"),
        TableUtils.createColumn("Product Type", "category.productType"),
        TableUtils.createColumn("Size", "category.size"),
        TableUtils.createColumn("Weight", "category.weight"),
        TableUtils.createColumn("Weight Unit", "category.weightUnit"),
        TableUtils.createColumn("Quantity", "quantity"),
        TableUtils.createFormattedColumn("Purchase Price", "purchasePrice"),
        TableUtils.createFormattedColumn("Selling Price", "sellingPrice")
    );

    TableUtils.populateTable(stockTable, columns, stocks);
  }

  private void setupSearch() {
    filteredData = new FilteredList<>(stockTable.getItems(), p -> true);
    stockTable.setItems(filteredData);
    stockSearchField.textProperty().addListener((observable, oldValue, newValue) -> stockSearch());
  }
}