package com.lestarieragemilang.desktop.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import com.jfoenix.controls.JFXButton;
import com.lestarieragemilang.desktop.model.Customer;
import com.lestarieragemilang.desktop.repository.GenericDao;
import com.lestarieragemilang.desktop.service.GenericService;
import com.lestarieragemilang.desktop.utils.ClearFields;
import com.lestarieragemilang.desktop.utils.GenericEditPopup;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import com.lestarieragemilang.desktop.utils.IdGenerator;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.TableUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerController extends HibernateUtil {
    @FXML
    private TableColumn<Customer, String> customerAddressCol, customerContactCol, customerEmailCol, customerIDCol,
            customerNameCol;
    @FXML
    private TextField customerAddressField, customerContactField, customerEmailField, customerIDIncrement,
            customerNameField, customerSearchField;
    @FXML
    private TableView<Customer> customerTable;
    @FXML
    private JFXButton editCustomerButtonText;

    private GenericService<Customer> customerService;

    public void initialize() {
        customerService = new GenericService<>(new GenericDao<>(Customer.class), "CUST");
        initializeCustomerTable();
        loadCustomers();
        generateAndSetCustomerId();

        // Disable the customer ID field
        customerIDIncrement.setDisable(true);
    }

    private void initializeCustomerTable() {
        List<TableColumn<Customer, ?>> columns = List.of(
                TableUtils.createColumn("Customer ID", "customerId"),
                TableUtils.createColumn("Name", "customerName"),
                TableUtils.createColumn("Contact", "contact"),
                TableUtils.createColumn("Address", "address"),
                TableUtils.createColumn("Email", "email"));
        TableUtils.populateTable(customerTable, columns, customerService.findAll());
    }

    private void loadCustomers() {
        List<Customer> customers = customerService.findAll();
        customerTable.setItems(FXCollections.observableArrayList(customers));
    }

    private void generateAndSetCustomerId() {
        String newCustomerId;
        do {
            newCustomerId = IdGenerator.generateRandomId("CUST", 1000);
        } while (customerIdExists(newCustomerId));

        customerIDIncrement.setText(newCustomerId);
    }

    private boolean customerIdExists(String customerId) {
        return customerService.findAll().stream()
                .anyMatch(customer -> customer.getCustomerId().equals(customerId));
    }

    @FXML
    private void addCustomerButton() {
        String customerId = customerIDIncrement.getText();
        if (customerIdExists(customerId)) {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "Duplicate Customer ID",
                    "Customer ID already exists.");
            generateAndSetCustomerId();
            return;
        }

        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setCustomerName(customerNameField.getText());
        customer.setContact(customerContactField.getText());
        customer.setAddress(customerAddressField.getText());
        customer.setEmail(customerEmailField.getText());

        customerService.save(customer);
        loadCustomers();
        resetCustomerButton();
    }

    @FXML
    private void resetCustomerButton() {
        ClearFields.clearFields(
                customerIDIncrement, customerNameField, customerContactField, customerAddressField, customerEmailField);
        generateAndSetCustomerId();
    }

    @FXML
    private void editCustomerButton() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "No Customer Selected",
                    "Please select a customer to edit.");
            return;
        }

        GenericEditPopup.create(Customer.class)
                .withTitle("Edit Customer")
                .forItem(selectedCustomer)
                .addField("Customer ID", new TextField(selectedCustomer.getCustomerId()), true)
                .addField("Name", new TextField(selectedCustomer.getCustomerName()))
                .addField("Contact", new TextField(selectedCustomer.getContact()))
                .addField("Address", new TextField(selectedCustomer.getAddress()))
                .addField("Email", new TextField(selectedCustomer.getEmail()))
                .onSave((customer, fields) -> {
                    customer.setCustomerName(((TextField) fields.get(1)).getText());
                    customer.setContact(((TextField) fields.get(2)).getText());
                    customer.setAddress(((TextField) fields.get(3)).getText());
                    customer.setEmail(((TextField) fields.get(4)).getText());
                    customerService.update(customer);
                })
                .afterSave(() -> {
                    loadCustomers();
                    resetCustomerButton();
                })
                .show();
    }

    @FXML
    private void removeCustomerButton() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            ShowAlert.showAlert(
                    AlertType.WARNING,
                    "No Customer Selected",
                    "Please select a customer to remove.");
            return;
        }

        customerService.delete(selectedCustomer);
        loadCustomers();
        resetCustomerButton();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = customerSearchField.getText().toLowerCase();
        List<Customer> allCustomers = customerService.findAll();
        List<Customer> filteredCustomers = allCustomers.stream()
                .filter(customer -> customer.getCustomerId().toLowerCase().contains(searchTerm) ||
                        customer.getCustomerName().toLowerCase().contains(searchTerm) ||
                        customer.getContact().toLowerCase().contains(searchTerm) ||
                        customer.getAddress().toLowerCase().contains(searchTerm) ||
                        customer.getEmail().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
        customerTable.setItems(FXCollections.observableArrayList(filteredCustomers));
    }
}