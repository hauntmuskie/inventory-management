package com.lestarieragemilang.desktop.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXRadioButton;
import com.lestarieragemilang.desktop.model.Purchasing;
import com.lestarieragemilang.desktop.model.Returns;
import com.lestarieragemilang.desktop.model.Sales;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.service.GenericService;
import com.lestarieragemilang.desktop.utils.ClearFields;
import com.lestarieragemilang.desktop.utils.GenericEditPopup;
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
    private TextField returnIDIncrement, searchTextField;
    @FXML
    private TextArea returnReasonField;
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
        returnService = new GenericService<>(new GenericDao<>(Returns.class), "RTN", 3);
        purchasingService = new GenericService<>(new GenericDao<>(Purchasing.class), "PCH", 3);
        salesService = new GenericService<>(new GenericDao<>(Sales.class), "SLS", 3);

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
        returnTypeGroup.selectedToggleProperty().addListener((_, _, newValue) -> {
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
            ShowAlert.showWarning("ID Retur sudah ada dalam sistem. ID baru akan dibuat.");
            generateAndSetReturnId();
            return;
        }

        Object selectedInvoice = returnInvoicePurchasing.getValue();
        if (selectedInvoice == null) {
            ShowAlert.showValidationError("Silakan pilih faktur terlebih dahulu.");
            return;
        }

        Returns returnItem = new Returns();
        returnItem.setReturnId(returnId);
        returnItem.setReturnDate(returnDate.getValue());

        if (selectedInvoice instanceof Purchasing) {
            returnItem.setInvoiceNumber(((Purchasing) selectedInvoice).getInvoiceNumber());
            returnItem.setReturnType("Buy");
        } else if (selectedInvoice instanceof Sales) {
            returnItem.setInvoiceNumber(((Sales) selectedInvoice).getInvoiceNumber());
            returnItem.setReturnType("Sell");
        } else {
            ShowAlert.showWarning("Silakan pilih faktur terlebih dahulu.");
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
            ShowAlert.showWarning("Silakan pilih data retur yang akan diubah.");
            return;
        }

        GenericEditPopup.create(Returns.class)
                .withTitle("Edit Return")
                .forItem(selectedReturn)
                .addField("Return ID", new TextField(selectedReturn.getReturnId()), true)
                .addField("Date", new DatePicker(selectedReturn.getReturnDate()))
                .addField("Invoice", new ComboBox<>(returnInvoicePurchasing.getItems()))
                .addField("Type", new TextField(selectedReturn.getReturnType()))
                .addField("Reason", new TextArea(selectedReturn.getReason()))
                .onSave((returnItem, fields) -> {
                    returnItem.setReturnDate(((DatePicker) fields.get(1)).getValue());

                    Object selectedInvoice = ((ComboBox<?>) fields.get(2)).getValue();
                    if (selectedInvoice == null) {
                        ShowAlert.showValidationError("Silakan pilih faktur terlebih dahulu.");
                        return;
                    }

                    if (selectedInvoice instanceof Purchasing) {
                        returnItem.setInvoiceNumber(((Purchasing) selectedInvoice).getInvoiceNumber());
                        returnItem.setReturnType("Buy");
                    } else if (selectedInvoice instanceof Sales) {
                        returnItem.setInvoiceNumber(((Sales) selectedInvoice).getInvoiceNumber());
                        returnItem.setReturnType("Sell");
                    } else {
                        ShowAlert.showWarning("Silakan pilih faktur terlebih dahulu.");
                        return;
                    }

                    returnItem.setReason(((TextField) fields.get(4)).getText());
                    returnService.update(returnItem);
                })
                .afterSave(() -> {
                    ShowAlert.showSuccess("Data retur berhasil diperbarui.");
                    loadReturns();
                    resetReturnButton();
                })
                .show();
    }

    @FXML
    private void removeReturnButton() {
        Returns selectedReturn = returnTable.getSelectionModel().getSelectedItem();
        if (selectedReturn == null) {
            ShowAlert.showWarning("Silakan pilih data retur yang akan dihapus.");
            return;
        }

        if (ShowAlert.showYesNo("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus data retur ini?")) {
            returnService.delete(selectedReturn);
            ShowAlert.showSuccess("Data retur berhasil dihapus.");
            loadReturns();
            resetReturnButton();
        }
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