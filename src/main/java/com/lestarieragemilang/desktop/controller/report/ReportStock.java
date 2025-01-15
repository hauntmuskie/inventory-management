package com.lestarieragemilang.desktop.controller.report;

import com.lestarieragemilang.desktop.model.Stock;
import com.lestarieragemilang.desktop.utils.*;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.hibernate.Session;
import java.net.URL;
import java.util.List;

public class ReportStock {
    @FXML private TableView<Stock> stockTable;
    @FXML private TextField stockSearchField;
    private FilteredList<Stock> filteredData;

    @FXML
    void printJasperStock(MouseEvent event) {
        String path = "/jasper/stock-list.jasper";
        URL url = getClass().getResource(path);
        if (url == null) url = getClass().getResource("/com/lestarieragemilang/desktop/jasper/stock-list.jasper");
        if (url == null) {
            ShowAlert.showError("Template laporan tidak ditemukan");
            return;
        }

        try {
            JasperLoader loader = new JasperLoader();
            String searchText = stockSearchField.getText();
            Stock stock = stockTable.getSelectionModel().getSelectedItem();

            if (searchText != null && !searchText.isEmpty()) {
                String wildcard = "%" + searchText + "%";
                loader.showJasperReportStock(url, wildcard, wildcard, wildcard, wildcard, 
                    wildcard, wildcard, wildcard, wildcard, event);
            } else if (stock != null) {
                loader.showJasperReportStock(url, stock.getCategory().getBrand(), 
                    stock.getCategory().getProductType(), stock.getCategory().getSize(),
                    stock.getCategory().getWeight().toString(), stock.getCategory().getWeightUnit(),
                    stock.getQuantity().toString(), stock.getPurchasePrice().toString(),
                    stock.getSellingPrice().toString(), event);
            } else {
                loader.showJasperReportStock(url, "%", "%", "%", "%", "%", "%", "%", "%", event);
            }
        } catch (Exception e) {
            ShowAlert.showError("Terjadi kesalahan saat membuat laporan: " + e.getMessage());
        }
    }

    @FXML
    void stockSearch() {
        String searchText = stockSearchField.getText().toLowerCase();
        filteredData.setPredicate(stock -> {
            if (searchText == null || searchText.isEmpty()) return true;
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
        setupTable(fetchStocksFromDatabase());
        setupSearch();
    }

    private List<Stock> fetchStocksFromDatabase() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Stock", Stock.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.emptyObservableList();
        }
    }

    private void setupTable(List<Stock> stocks) {
        List<TableColumn<Stock, ?>> columns = List.of(
            TableUtils.createColumn("Kode Stok", "stockId"),
            TableUtils.createColumn("Merek", "category.brand"),
            TableUtils.createColumn("Tipe Produk", "category.productType"),
            TableUtils.createColumn("Ukuran", "category.size"),
            TableUtils.createColumn("Berat", "category.weight"),
            TableUtils.createColumn("Satuan Berat", "category.weightUnit"),
            TableUtils.createColumn("Jumlah", "quantity"),
            TableUtils.createFormattedColumn("Harga Beli", "purchasePrice"),
            TableUtils.createFormattedColumn("Harga Jual", "sellingPrice")
        );
        TableUtils.populateTable(stockTable, columns, stocks);
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(stockTable.getItems(), _ -> true);
        stockTable.setItems(filteredData);
        stockSearchField.textProperty().addListener((_, _, _) -> stockSearch());
    }
}