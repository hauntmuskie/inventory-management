package com.lestarieragemilang.desktop.controller.report;

import com.lestarieragemilang.desktop.model.Returns;
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

public class ReportReturn {

    @FXML
    private TableView<Returns> returnTable;
    @FXML
    private TextField returnSearchField;

    private FilteredList<Returns> filteredData;

    @FXML
    void printJasperReturn(MouseEvent event) {
        try {
            String path = "/jasper/returns-list.jasper";
            URL url = getClass().getResource(path);
            if (url == null) {
                path = "/com/lestarieragemilang/desktop/jasper/returns-list.jasper";
                url = getClass().getResource(path);
            }

            if (url == null) {
                throw new RuntimeException("Could not find report template");
            }

            Returns selectedReturn = returnTable.getSelectionModel().getSelectedItem();
            if (selectedReturn == null) {
                throw new RuntimeException("Please select a return to print");
            }

            JasperLoader loader = new JasperLoader();
            loader.showJasperReportReturn(url, 
                selectedReturn.getReturnId(),
                selectedReturn.getReturnDate(),
                selectedReturn.getReturnType(), 
                selectedReturn.getInvoiceNumber(),
                selectedReturn.getReason(),
                event
            );

        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    void returnSearch() {
        String searchText = returnSearchField.getText().toLowerCase();
        filteredData.setPredicate(returns -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            return returns.getReturnId().toLowerCase().contains(searchText) ||
                returns.getReturnType().toLowerCase().contains(searchText) ||
                returns.getInvoiceNumber().toLowerCase().contains(searchText) ||
                returns.getReason().toLowerCase().contains(searchText);
        });
    }

    @FXML
    void initialize() {
        List<Returns> returns = fetchReturnsFromDatabase();
        setupTable(returns);
        setupSearch();
    }

    private List<Returns> fetchReturnsFromDatabase() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Returns> query = session.createQuery("FROM Returns", Returns.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return FXCollections.emptyObservableList();
        }
    }

    private void setupTable(List<Returns> returns) {
        List<TableColumn<Returns, ?>> columns = List.of(
            TableUtils.createColumn("Return ID", "returnId"),
            TableUtils.createColumn("Return Date", "returnDate"),
            TableUtils.createColumn("Return Type", "returnType"),
            TableUtils.createColumn("Invoice Number", "invoiceNumber"),
            TableUtils.createColumn("Reason", "reason")
        );

        TableUtils.populateTable(returnTable, columns, returns);
    }

    private void setupSearch() {
        filteredData = new FilteredList<>(returnTable.getItems(), p -> true);
        returnTable.setItems(filteredData);
        returnSearchField.textProperty().addListener((observable, oldValue, newValue) -> returnSearch());
    }
}
