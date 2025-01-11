package com.lestarieragemilang.desktop.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.lestarieragemilang.desktop.model.Supplier;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.service.GenericService;
import com.lestarieragemilang.desktop.utils.ClearFields;
import com.lestarieragemilang.desktop.utils.GenericEditPopup;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.IdGenerator;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.TableUtils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

public class SupplierController extends HibernateUtil {
    @FXML
    private JFXButton editSupplierButtonText;
    @FXML
    private TableColumn<Supplier, String> supplierAddressCol, supplierContactCol, supplierEmailCol, supplierIDCol,
            supplierNameCol;
    @FXML
    private JFXTextArea supplierAddressField;
    @FXML
    private VBox supplierAddressField1, supplierAddressField11;
    @FXML
    private TextField supplierContactField, supplierEmailField, supplierIDIncrement, supplierNameField,
            supplierSearchField;
    @FXML
    private TableView<Supplier> supplierTable;

    private GenericService<Supplier> supplierService;

    public void initialize() {
        supplierService = new GenericService<>(new GenericDao<>(Supplier.class), "PMS", 3);  // Changed from SUP to PMS

        initializeSupplierTable();
        loadSuppliers();
        generateAndSetSupplierId();

        // Disable the supplier ID field
        supplierIDIncrement.setDisable(true);
    }

    private void initializeSupplierTable() {
        List<TableColumn<Supplier, ?>> columns = List.of(
                TableUtils.createColumn("Kode Pemasok", "supplierId"),
                TableUtils.createColumn("Nama", "supplierName"),
                TableUtils.createColumn("Kontak", "contact"),
                TableUtils.createColumn("Surel", "email"),
                TableUtils.createColumn("Alamat", "address"));
        TableUtils.populateTable(supplierTable, columns, supplierService.findAll());
    }

    private void loadSuppliers() {
        List<Supplier> suppliers = supplierService.findAll();
        supplierTable.setItems(FXCollections.observableArrayList(suppliers));
    }

    private void generateAndSetSupplierId() {
        String newSupplierId;
        do {
            newSupplierId = IdGenerator.generateRandomId("PMS", 1000);  // Changed from SUP to PMS
        } while (supplierIdExists(newSupplierId));

        supplierIDIncrement.setText(newSupplierId);
    }

    private boolean supplierIdExists(String supplierId) {
        return supplierService.findAll().stream()
                .anyMatch(supplier -> supplier.getSupplierId().equals(supplierId));
    }

    @FXML
    private void addSupplierButton(ActionEvent event) {
        String supplierId = supplierIDIncrement.getText();
        if (supplierIdExists(supplierId)) {
            ShowAlert.showWarning("ID Pemasok sudah ada di database");
            generateAndSetSupplierId();
            return;
        }

        Supplier supplier = new Supplier();
        supplier.setSupplierId(supplierId);
        supplier.setSupplierName(supplierNameField.getText());
        supplier.setContact(supplierContactField.getText());
        supplier.setEmail(supplierEmailField.getText());
        supplier.setAddress(supplierAddressField.getText());

        supplierService.save(supplier);
        ShowAlert.showSuccess("Data pemasok berhasil ditambahkan");
        loadSuppliers();
        resetSupplierButton();
    }

    @FXML
    private void editSupplierButton(ActionEvent event) {
        Supplier selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
        if (selectedSupplier == null) {
            ShowAlert.showWarning("Silakan pilih pemasok yang akan diubah");
            return;
        }

        GenericEditPopup.create(Supplier.class)
                .withTitle("Ubah Pemasok")
                .forItem(selectedSupplier)
                .addField("Kode Pemasok", new TextField(selectedSupplier.getSupplierId()), true)
                .addField("Nama", new TextField(selectedSupplier.getSupplierName()))
                .addField("Kontak", new TextField(selectedSupplier.getContact()))
                .addField("Surel", new TextField(selectedSupplier.getEmail()))
                .addField("Alamat", new TextField(selectedSupplier.getAddress()))
                .onSave((supplier, fields) -> {
                    supplier.setSupplierName(((TextField) fields.get(1)).getText());
                    supplier.setContact(((TextField) fields.get(2)).getText());
                    supplier.setEmail(((TextField) fields.get(3)).getText());
                    supplier.setAddress(((TextField) fields.get(4)).getText());
                    supplierService.update(supplier);
                })
                .afterSave(() -> {
                    ShowAlert.showSuccess("Data pemasok berhasil diubah");
                    loadSuppliers();
                    resetSupplierButton();
                    supplierTable.refresh(); // Add this line
                })
                .show();
    }

    @FXML
    private void removeSupplierButton(ActionEvent event) {
        Supplier selectedSupplier = supplierTable.getSelectionModel().getSelectedItem();
        if (selectedSupplier == null) {
            ShowAlert.showWarning("Silakan pilih pemasok yang akan dihapus");
            return;
        }

        if (ShowAlert.showYesNo("Konfirmasi Hapus", "Apakah Anda yakin ingin menghapus data pemasok ini?")) {
            supplierService.delete(selectedSupplier);
            ShowAlert.showSuccess("Data pemasok berhasil dihapus");
            loadSuppliers();
            resetSupplierButton();
        }
    }

    @FXML
    private void resetSupplierButton() {
        ClearFields.clearFields(
                supplierIDIncrement, supplierNameField, supplierContactField, supplierEmailField, supplierAddressField);
        generateAndSetSupplierId();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = supplierSearchField.getText().toLowerCase();
        List<Supplier> allSuppliers = supplierService.findAll();
        List<Supplier> filteredSuppliers = allSuppliers.stream()
                .filter(supplier -> supplier.getSupplierId().toLowerCase().contains(searchTerm) ||
                        supplier.getSupplierName().toLowerCase().contains(searchTerm) ||
                        supplier.getContact().toLowerCase().contains(searchTerm) ||
                        supplier.getEmail().toLowerCase().contains(searchTerm) ||
                        supplier.getAddress().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
        supplierTable.setItems(FXCollections.observableArrayList(filteredSuppliers));
    }
}