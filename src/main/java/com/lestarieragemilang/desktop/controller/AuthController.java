package com.lestarieragemilang.desktop.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AuthController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private StackPane authStackPane;

    @FXML
    private PasswordField confirmPassword;

    @FXML
    private Label greetingText;

    @FXML
    private PasswordField loginPassword;

    @FXML
    private TextField loginUsername;

    @FXML
    private VBox loginView;

    @FXML
    private ListView<?> profileListView;

    @FXML
    private TextField registerEmail;

    @FXML
    private PasswordField registerPassword;

    @FXML
    private TextField registerUsername;

    @FXML
    private VBox registerView;

    @FXML
    void deleteAdmin(ActionEvent event) {

    }

    @FXML
    void exitApp(ActionEvent event) {

    }

    @FXML
    void loginToApp(ActionEvent event) {

    }

    @FXML
    void registerAdmin(ActionEvent event) {

    }

    @FXML
    void showLoginView(ActionEvent event) {

    }

    @FXML
    void showRegisterView(ActionEvent event) {

    }

}
