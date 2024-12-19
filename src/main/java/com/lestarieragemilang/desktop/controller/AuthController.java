package com.lestarieragemilang.desktop.controller;

import com.lestarieragemilang.desktop.model.User;
import com.lestarieragemilang.desktop.service.UserService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.UUID;

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
    private ListView<String> profileListView;

    @FXML
    private TextField registerEmail;

    @FXML
    private PasswordField registerPassword;

    @FXML
    private TextField registerUsername;

    @FXML
    private VBox registerView;

    private final UserService userService;

    public AuthController() {
        this.userService = new UserService();
    }

    @FXML
    public void initialize() {
        loadUsers();
    }

    private void loadUsers() {
        profileListView.setItems(FXCollections.observableArrayList(
                userService.findAll().stream()
                        .map(User::getUsername)
                        .toList()));
    }

    @FXML
    void loginToApp(ActionEvent event) {
        String username = loginUsername.getText();
        String password = loginPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Please fill in all fields");
            return;
        }

        User user = userService.authenticate(username, password);
        if (user != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lestarieragemilang/desktop/layout.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);
                Stage stage = (Stage) loginView.getScene().getWindow();
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not load main view");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid username or password");
        }
    }

    @FXML
    void showLoginView(ActionEvent event) {
        loginView.setVisible(true);
        registerView.setVisible(false);
    }

    @FXML
    void exitApp(ActionEvent event) {
        Platform.exit();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
