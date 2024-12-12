package com.lestarieragemilang.desktop.controller;

import com.lestarieragemilang.desktop.model.Stock;
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
    String path = "/com/lestarieragemilang/desktop/jasper/stock-list.jasper";
    try {
      URL url = getClass().getResource(path);
      JasperLoader loader = new JasperLoader();
      loader.showJasperReportStock(
          url,
          stockSearchField.getText(),
          stockSearchField.getText(),
          stockSearchField.getText(),
          stockSearchField.getText(),
          stockSearchField.getText(),
          stockSearchField.getText(),
          stockSearchField.getText(),
          stockSearchField.getText());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @FXML
    void stockSearch() {
        String searchText = stockSearchField.getText().toLowerCase();
        filteredData.setPredicate(stock ->
            searchText.isEmpty() || 
            stock.getCategory().getBrand().toLowerCase().contains(searchText) ||
            stock.getCategory().getProductType().toLowerCase().contains(searchText) ||
            stock.getCategory().getSize().toLowerCase().contains(searchText) ||
            stock.getCategory().getWeightUnit().toLowerCase().contains(searchText) ||
            stock.getQuantity().toString().toLowerCase().contains(searchText) ||
            stock.getPurchasePrice().toString().toLowerCase().contains(searchText) ||
            stock.getSellingPrice().toString().toLowerCase().contains(searchText)
        );
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
        TableUtils.createColumn("Brand", "category.brand"),
        TableUtils.createFormattedColumn("Buy Price", "purchasePrice"),
        TableUtils.createColumn("Stock ID", "stockId"),
        TableUtils.createColumn("Category ID", "category.id"),
        TableUtils.createColumn("Quantity", "quantity"),
        TableUtils.createFormattedColumn("Sell Price", "sellingPrice"),
        TableUtils.createColumn("Size", "category.size"),
        TableUtils.createColumn("Type", "category.type"),
        TableUtils.createColumn("Unit", "category.unit"),
        TableUtils.createColumn("Weight", "category.weight"));

    TableUtils.populateTable(stockTable, columns, stocks);
  }

  private void setupSearch() {
    filteredData = new FilteredList<>(stockTable.getItems(), p -> true);
    stockTable.setItems(filteredData);
    stockSearchField.textProperty().addListener((observable, oldValue, newValue) -> stockSearch());
  }
}