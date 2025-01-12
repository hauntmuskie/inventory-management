package com.lestarieragemilang.desktop.controller;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.lestarieragemilang.desktop.App;
import com.lestarieragemilang.desktop.model.User;
import com.lestarieragemilang.desktop.service.UserService;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
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

import java.io.IOException;
import java.util.Optional;

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
    private final EventBus eventBus;

    public AuthController() {
        this.userService = new UserService();
        this.eventBus = new EventBus();
    }

    @FXML
    public void initialize() {
        loadUsers();
        eventBus.register(this);
    }

    private void loadUsers() {
        try {
            ImmutableList<String> usernames = userService.findAll().stream()
                    .map(User::getUsername)
                    .collect(ImmutableList.toImmutableList());

            profileListView.setItems(FXCollections.observableArrayList(usernames));
        } catch (Exception e) {
            ShowAlert.showDatabaseError("Gagal memuat daftar pengguna: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void loginToApp(ActionEvent event) {
        try {
            validateLoginInput();
            
            if (!ShowAlert.showConfirmation("Konfirmasi Login", "Konfirmasi Masuk", 
                "Apakah Anda yakin ingin masuk dengan akun ini?")) {
                return;
            }

            Optional<User> user = authenticateUser();
            user.ifPresentOrElse(
                    u -> {
                        handleSuccessfulLogin(u);
                        ShowAlert.showSuccess("Berhasil masuk ke sistem");
                    },
                    () -> ShowAlert.showError("Nama pengguna atau kata sandi tidak valid"));
        } catch (IllegalArgumentException e) {
            ShowAlert.showValidationError(e.getMessage());
        } catch (Exception e) {
            ShowAlert.showDatabaseError("Terjadi kesalahan saat masuk: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateLoginInput() {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(loginUsername.getText()), "Nama pengguna tidak boleh kosong");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(loginPassword.getText()), "Kata sandi tidak boleh kosong");
    }

    private Optional<User> authenticateUser() {
        return Optional.ofNullable(
                userService.authenticate(loginUsername.getText(), loginPassword.getText()));
    }

    private void handleSuccessfulLogin(User user) {
        try {
            Stage currentStage = (Stage) loginView.getScene().getWindow();
            App.sceneManager.invalidateScene("layout");

            switchScene(anchorPane, "layout", () -> {
                try {
                    Parent layoutRoot = App.sceneManager.getScene("layout");
                    currentStage.setScene(new Scene(layoutRoot));
                    currentStage.show();
                } catch (IOException e) {
                    ShowAlert.showError("Gagal memuat tampilan utama: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            ShowAlert.showError("Terjadi kesalahan saat memuat tampilan utama: " + e.getMessage());
            e.printStackTrace();
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
            ShowAlert.showError("Gagal memuat tampilan masuk");
        }
    }

    @Override
    protected void animateFadeIn(Parent node) {
        Preconditions.checkNotNull(node, "Node cannot be null");
        node.setOpacity(0);
        new animatefx.animation.FadeIn(node).play();
    }

    @Override
    protected void animateFadeOut(Parent node, Runnable onFinished) {
        Preconditions.checkNotNull(node, "Node cannot be null");
        Preconditions.checkNotNull(onFinished, "Callback cannot be null");

        animatefx.animation.FadeOut fadeOut = new animatefx.animation.FadeOut(node);
        fadeOut.setOnFinished(_ -> onFinished.run());
        fadeOut.play();
    }

    @FXML
    void exitApp(ActionEvent event) {
        if (ShowAlert.showYesNo("Konfirmasi Keluar", 
            "Apakah Anda yakin ingin keluar dari aplikasi?")) {
            HibernateUtil.shutdown();
            Platform.exit();
        }
    }
}
