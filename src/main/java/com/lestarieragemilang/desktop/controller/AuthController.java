package com.lestarieragemilang.desktop.controller;

import java.io.IOException;

import com.lestarieragemilang.desktop.App;
import com.lestarieragemilang.desktop.model.User;
import com.lestarieragemilang.desktop.service.UserService;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.Redirect;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class AuthController extends Redirect {

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
            ShowAlert.showValidationError("Mohon isi semua field yang diperlukan");
            return;
        }

        User user = userService.authenticate(username, password);
        if (user != null) {
            try {
                Stage currentStage = (Stage) loginView.getScene().getWindow();
                // Invalidate layout scene before loading
                App.sceneManager.invalidateScene("layout");
                
                switchScene(anchorPane, "layout", () -> {
                    try {
                        Parent layoutRoot = App.sceneManager.getScene("layout");
                        Scene layoutScene = new Scene(layoutRoot);
                        currentStage.setScene(layoutScene);
                        currentStage.show();
                    } catch (IOException e) {
                        ShowAlert.showError("Gagal memuat tampilan utama");
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                ShowAlert.showError("Terjadi kesalahan saat memuat tampilan utama");
                e.printStackTrace();
            }
        } else {
            ShowAlert.showError("Username atau password tidak valid");
        }
    }

    @FXML
    void showLoginView(ActionEvent event) {
        try {
            Parent loginRoot = App.sceneManager.getScene("login");
            loginView.getChildren().setAll(loginRoot);
            loginView.setVisible(true);
            registerView.setVisible(false);
            animateFadeIn(loginView);
        } catch (IOException e) {
            ShowAlert.showError("Gagal memuat tampilan login");
        }
    }

    @Override
    protected void animateFadeIn(Parent node) {
        node.setOpacity(0);
        new animatefx.animation.FadeIn(node).play();
    }

    @Override
    protected void animateFadeOut(Parent node, Runnable onFinished) {
        animatefx.animation.FadeOut fadeOut = new animatefx.animation.FadeOut(node);
        fadeOut.setOnFinished(_ -> onFinished.run());
        fadeOut.play();
    }

    @FXML
    void exitApp(ActionEvent event) {
        Platform.exit();
    }
}
