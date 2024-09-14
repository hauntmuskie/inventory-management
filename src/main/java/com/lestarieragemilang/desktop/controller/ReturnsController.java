package com.lestarieragemilang.desktop.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.util.StringConverter;
import org.hibernate.SessionFactory;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import com.lestarieragemilang.desktop.model.Purchasing;
import com.lestarieragemilang.desktop.model.Returns;
import com.lestarieragemilang.desktop.model.Sales;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.service.GenericService;
import com.lestarieragemilang.desktop.utils.ClearFields;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.IdGenerator;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.TableUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReturnsController extends HibernateUtil {
    @FXML
    private JFXButton editReturnButtonText;
    @FXML
    private DatePicker returnDate;
    @FXML
    private TextField returnIDIncrement, returnReasonField, searchTextField;
    @FXML
    private JFXComboBox<Object> returnInvoicePurchasing;
    @FXML
    private JFXRadioButton returnIsBuy, returnIsSell;
    @FXML
    private TableView<Returns> returnTable;
    @FXML
    private TableColumn<Returns, String> returnIDCol, returnInvoiceCol, returnTypeCol, returnReasonCol;
    @FXML
    private TableColumn<Returns, LocalDate> returnDateCol;

    private GenericService<Returns> returnService;
    private GenericService<Purchasing> purchasingService;
    private GenericService<Sales> salesService;

    public void initialize() {
        SessionFactory sessionFactory = getSessionFactory();

        returnService = new GenericService<>(new GenericDao<>(Returns.class, sessionFactory), "RTN");
        purchasingService = new GenericService<>(new GenericDao<>(Purchasing.class, sessionFactory), "PCH");
        salesService = new GenericService<>(new GenericDao<>(Sales.class, sessionFactory), "SLS");

        initializeInvoiceComboBox();
        initializeReturnTable();
        loadReturns();
        generateAndSetReturnId();

        // Disable the return ID field
        returnIDIncrement.setDisable(true);

        // Set up radio button group
        ToggleGroup returnTypeGroup = new ToggleGroup();
        returnIsBuy.setToggleGroup(returnTypeGroup);
        returnIsSell.setToggleGroup(returnTypeGroup);

        // Add listener to toggle group
        returnTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                updateInvoiceComboBox();
            }
        });
    }

    private void initializeInvoiceComboBox() {
        returnInvoicePurchasing.setConverter(new StringConverter<Object>() {
            @Override
            public String toString(Object object) {
                if (object instanceof Purchasing) {
                    return ((Purchasing) object).getInvoiceNumber();
                } else if (object instanceof Sales) {
                    return ((Sales) object).getInvoiceNumber();
                }
                return "";
            }

            @Override
            public Object fromString(String string) {
                return null;
            }
        });
    }

    private void updateInvoiceComboBox() {
        if (returnIsBuy.isSelected()) {
            List<Purchasing> purchasings = purchasingService.findAll();
            returnInvoicePurchasing.setItems(FXCollections.observableArrayList(purchasings));
        } else if (returnIsSell.isSelected()) {
            List<Sales> sales = salesService.findAll();
            returnInvoicePurchasing.setItems(FXCollections.observableArrayList(sales));
        }
    }

    private void initializeReturnTable() {
        List<TableColumn<Returns, ?>> columns = List.of(
                TableUtils.createColumn("Return ID", "returnId"),
                TableUtils.createColumn("Date", "returnDate"),
                TableUtils.createColumn("Invoice", "invoiceNumber"),
                TableUtils.createColumn("Type", "returnType"),
                TableUtils.createColumn("Reason", "reason"));
        TableUtils.populateTable(returnTable, columns, returnService.findAll());
    }

    private void loadReturns() {
        List<Returns> returns = returnService.findAll();
        returnTable.setItems(FXCollections.observableArrayList(returns));
    }

    private void generateAndSetReturnId() {
        String newReturnId;
        do {
            newReturnId = IdGenerator.generateRandomId("RTN", 1000);
        } while (returnIdExists(newReturnId));

        returnIDIncrement.setText(newReturnId);
    }

    private boolean returnIdExists(String returnId) {
        return returnService.findAll().stream()
                .anyMatch(returnItem -> returnItem.getReturnId().equals(returnId));
    }

    @FXML
    private void addReturnButton() {
        String returnId = returnIDIncrement.getText();
        if (returnIdExists(returnId)) {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "Duplicate Return ID",
                    "Return ID already exists.");
            generateAndSetReturnId();
            return;
        }

        Returns returnItem = new Returns();
        returnItem.setReturnId(returnId);
        returnItem.setReturnDate(returnDate.getValue());

        Object selectedInvoice = returnInvoicePurchasing.getValue();
        if (selectedInvoice instanceof Purchasing) {
            returnItem.setInvoiceNumber(((Purchasing) selectedInvoice).getInvoiceNumber());
            returnItem.setReturnType("Buy");
        } else if (selectedInvoice instanceof Sales) {
            returnItem.setInvoiceNumber(((Sales) selectedInvoice).getInvoiceNumber());
            returnItem.setReturnType("Sell");
        } else {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "Missing Invoice",
                    "Please select an invoice.");
            return;
        }

        returnItem.setReason(returnReasonField.getText());

        returnService.save(returnItem);
        loadReturns();
        resetReturnButton();
    }

    @FXML
    private void resetReturnButton() {
        ClearFields.clearFields(
                returnIDIncrement, returnDate, returnInvoicePurchasing, returnReasonField);
        returnIsBuy.setSelected(false);
        returnIsSell.setSelected(false);
        generateAndSetReturnId();
    }

    @FXML
    private void editReturnButton() {
        Returns selectedReturn = returnTable.getSelectionModel().getSelectedItem();
        if (selectedReturn == null) {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "No Return Selected",
                    "Please select a return to edit.");
            return;
        }

        selectedReturn.setReturnDate(returnDate.getValue());

        Object selectedInvoice = returnInvoicePurchasing.getValue();
        if (selectedInvoice instanceof Purchasing) {
            selectedReturn.setInvoiceNumber(((Purchasing) selectedInvoice).getInvoiceNumber());
            selectedReturn.setReturnType("Buy");
        } else if (selectedInvoice instanceof Sales) {
            selectedReturn.setInvoiceNumber(((Sales) selectedInvoice).getInvoiceNumber());
            selectedReturn.setReturnType("Sell");
        } else {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "Missing Invoice",
                    "Please select an invoice.");
            return;
        }

        selectedReturn.setReason(returnReasonField.getText());

        returnService.update(selectedReturn);
        loadReturns();
        resetReturnButton();
    }

    @FXML
    private void removeReturnButton() {
        Returns selectedReturn = returnTable.getSelectionModel().getSelectedItem();
        if (selectedReturn == null) {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "No Return Selected",
                    "Please select a return to remove.");
            return;
        }

        returnService.delete(selectedReturn);
        loadReturns();
        resetReturnButton();
    }

    @FXML
    private void searchingData() {
        String searchTerm = searchTextField.getText().toLowerCase();
        List<Returns> allReturns = returnService.findAll();
        List<Returns> filteredReturns = allReturns.stream()
                .filter(returnItem -> returnItem.getReturnId().toLowerCase().contains(searchTerm) ||
                        returnItem.getInvoiceNumber().toLowerCase().contains(searchTerm) ||
                        returnItem.getReturnType().toLowerCase().contains(searchTerm) ||
                        returnItem.getReason().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
        returnTable.setItems(FXCollections.observableArrayList(filteredReturns));
    }
}