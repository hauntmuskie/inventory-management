package com.lestarieragemilang.desktop.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
        returnService = new GenericService<>(new GenericDao<>(Returns.class), "RET", 3);
        purchasingService = new GenericService<>(new GenericDao<>(Purchasing.class), "BLI", 3);
        salesService = new GenericService<>(new GenericDao<>(Sales.class), "JUL", 3);

        initializeInvoiceComboBox();
        initializeReturnTable();
        loadReturns();
        generateAndSetReturnId();

        returnIDIncrement.setDisable(true);

        ToggleGroup returnTypeGroup = new ToggleGroup();
        returnIsBuy.setToggleGroup(returnTypeGroup);
        returnIsSell.setToggleGroup(returnTypeGroup);

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
                    String fullInvoice = ((Purchasing) object).getInvoiceNumber();
                    return formatInvoiceNumber(fullInvoice);
                } else if (object instanceof Sales) {
                    String fullInvoice = ((Sales) object).getInvoiceNumber();
                    return formatInvoiceNumber(fullInvoice);
                }
                return "";
            }

            @Override
            public Object fromString(String string) {
                return null;
            }
        });
    }

    // Add this helper method to format invoice numbers
    private String formatInvoiceNumber(String fullInvoice) {
        String[] parts = fullInvoice.split("-");
        if (parts.length > 0) {
            String prefix = parts[0];
            String lastPart = parts[parts.length - 1];
            return prefix + "-" + lastPart;
        }
        return fullInvoice;
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
                TableUtils.createColumn("Kode Retur", "returnId"),
                TableUtils.createColumn("Tanggal", "returnDate"),
                TableUtils.createColumn("No. Faktur", "invoiceNumber"),
                TableUtils.createColumn("Tipe", "returnType"),
                TableUtils.createColumn("Alasan", "reason"));
        TableUtils.populateTable(returnTable, columns, returnService.findAll());
    }

    private void loadReturns() {
        List<Returns> returns = returnService.findAll();
        returnTable.setItems(FXCollections.observableArrayList(returns));
    }

    private void generateAndSetReturnId() {
        String newReturnId;
        do {
            newReturnId = IdGenerator.generateRandomId("RET", 1000);
        } while (returnIdExists(newReturnId));

        returnIDIncrement.setText(newReturnId);
    }

    private boolean returnIdExists(String returnId) {
        return returnService.findAll().stream()
                .anyMatch(returnItem -> returnItem.getReturnId().equals(returnId));
    }

    @FXML
    private void addReturnButton() {
        if (!ShowAlert.showYesNo("Konfirmasi Tambah", "Apakah Anda yakin ingin menambah data retur ini?")) {
            return;
        }

        String returnId = returnIDIncrement.getText();
        if (returnIdExists(returnId)) {
            ShowAlert.showWarning("Kode Retur sudah ada dalam sistem. Kode baru akan dibuat.");
            generateAndSetReturnId();
            return;
        }

        if (returnInvoicePurchasing.getValue() == null) {
            ShowAlert.showValidationError("Silakan pilih faktur terlebih dahulu.");
            return;
        }

        try {
            Returns returnItem = new Returns();
            returnItem.setReturnId(returnId);
            returnItem.setReturnDate(returnDate.getValue());

            Object selectedInvoice = returnInvoicePurchasing.getValue();
            if (selectedInvoice instanceof Purchasing) {
                returnItem.setInvoiceNumber(((Purchasing) selectedInvoice).getInvoiceNumber());
                returnItem.setReturnType("Beli");
            } else if (selectedInvoice instanceof Sales) {
                returnItem.setInvoiceNumber(((Sales) selectedInvoice).getInvoiceNumber());
                returnItem.setReturnType("Jual");
            } else {
                ShowAlert.showWarning("Silakan pilih faktur terlebih dahulu.");
                return;
            }

            returnItem.setReason(returnReasonField.getText());
            returnService.save(returnItem);
            ShowAlert.showSuccess("Data retur berhasil ditambahkan");
            loadReturns();
            resetReturnButton();
        } catch (Exception e) {
            ShowAlert.showError("Gagal menambahkan data retur: " + e.getMessage());
        }
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

        JFXRadioButton returnIsBuyEdit = new JFXRadioButton("Beli");
        JFXRadioButton returnIsSellEdit = new JFXRadioButton("Jual");
        ToggleGroup returnTypeGroupEdit = new ToggleGroup();
        returnIsBuyEdit.setToggleGroup(returnTypeGroupEdit);
        returnIsSellEdit.setToggleGroup(returnTypeGroupEdit);

        // Set initial radio button selection based on return type
        if ("Beli".equals(selectedReturn.getReturnType())) {
            returnIsBuyEdit.setSelected(true);
        } else {
            returnIsSellEdit.setSelected(true);
        }

        ComboBox<Object> invoiceComboBox = new ComboBox<>();
        invoiceComboBox.setConverter(new StringConverter<Object>() {
            @Override
            public String toString(Object object) {
                if (object instanceof Purchasing) {
                    String fullInvoice = ((Purchasing) object).getInvoiceNumber();
                    return formatInvoiceNumber(fullInvoice);
                } else if (object instanceof Sales) {
                    String fullInvoice = ((Sales) object).getInvoiceNumber();
                    return formatInvoiceNumber(fullInvoice);
                }
                return "";
            }

            @Override
            public Object fromString(String string) {
                return null;
            }
        });

        // Update invoice combo box when radio selection changes
        returnTypeGroupEdit.selectedToggleProperty().addListener((_, _, _) -> {
            if (returnIsBuyEdit.isSelected()) {
                List<Purchasing> purchasings = purchasingService.findAll();
                invoiceComboBox.setItems(FXCollections.observableArrayList(purchasings));
            } else {
                List<Sales> sales = salesService.findAll();
                invoiceComboBox.setItems(FXCollections.observableArrayList(sales));
            }
        });

        // Trigger initial load of invoice list
        if (returnIsBuyEdit.isSelected()) {
            List<Purchasing> purchasings = purchasingService.findAll();
            invoiceComboBox.setItems(FXCollections.observableArrayList(purchasings));
        } else {
            List<Sales> sales = salesService.findAll();
            invoiceComboBox.setItems(FXCollections.observableArrayList(sales));
        }

        // Set initial invoice selection
        invoiceComboBox.getItems().forEach(item -> {
            String invoiceNumber = item instanceof Purchasing ? 
                ((Purchasing) item).getInvoiceNumber() : 
                ((Sales) item).getInvoiceNumber();
            if (invoiceNumber.equals(selectedReturn.getInvoiceNumber())) {
                invoiceComboBox.setValue(item);
            }
        });

        VBox returnTypeBox = new VBox(5, returnIsBuyEdit, returnIsSellEdit);

        GenericEditPopup.create(Returns.class)
                .withTitle("Ubah Retur")
                .forItem(selectedReturn)
                .addField("Kode Retur", new TextField(selectedReturn.getReturnId()), true)
                .addField("Tanggal", new DatePicker(selectedReturn.getReturnDate()))
                .addField("Tipe Retur", returnTypeBox)
                .addField("Faktur", invoiceComboBox)
                .addField("Alasan", new TextArea(selectedReturn.getReason()))
                .onSave((returnItem, fields) -> {
                    if (!ShowAlert.showYesNo("Konfirmasi Ubah", "Apakah Anda yakin ingin mengubah data retur ini?")) {
                        return;
                    }

                    try {
                        returnItem.setReturnDate(((DatePicker) fields.get(1)).getValue());

                        Object selectedInvoice = ((ComboBox<?>) fields.get(3)).getValue();
                        if (selectedInvoice == null) {
                            ShowAlert.showValidationError("Silakan pilih faktur terlebih dahulu.");
                            return;
                        }

                        if (selectedInvoice instanceof Purchasing) {
                            returnItem.setInvoiceNumber(((Purchasing) selectedInvoice).getInvoiceNumber());
                            returnItem.setReturnType("Beli");
                        } else if (selectedInvoice instanceof Sales) {
                            returnItem.setInvoiceNumber(((Sales) selectedInvoice).getInvoiceNumber());
                            returnItem.setReturnType("Jual");
                        }

                        returnItem.setReason(((TextArea) fields.get(4)).getText());
                        returnService.update(returnItem);
                        ShowAlert.showSuccess("Data retur berhasil diperbarui");
                    } catch (Exception e) {
                        ShowAlert.showError("Gagal mengubah data retur: " + e.getMessage());
                    }
                })
                .afterSave(() -> {
                    loadReturns();
                    resetReturnButton();
                    returnTable.refresh();
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

        if (!ShowAlert.showYesNo("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus data retur ini?")) {
            return;
        }

        try {
            returnService.delete(selectedReturn);
            ShowAlert.showSuccess("Data retur berhasil dihapus");
            loadReturns();
            resetReturnButton();
        } catch (Exception e) {
            ShowAlert.showError("Gagal menghapus data retur: " + e.getMessage());
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