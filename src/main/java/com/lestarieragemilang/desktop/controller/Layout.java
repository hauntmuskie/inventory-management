package com.lestarieragemilang.desktop.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import com.lestarieragemilang.desktop.utils.Redirect;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Layout {

    @FXML
    private AnchorPane setScene;
    @FXML
    private Label localTimeApp;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy '['HH:mm:ss']'",
            new Locale.Builder().setLanguageTag("id").build());

    @FXML
    public void initialize() throws IOException {
        loadScene("stock");
        showDateAndTime();
    }

    private void showDateAndTime() {
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime currentTime = LocalDateTime.now();
            localTimeApp.setText(currentTime.format(formatter));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    @FXML
    void isExitApp(MouseEvent event) {
        confirmAction("Exit", "Apakah Anda yakin ingin keluar?", () -> System.exit(0));
    }

    @FXML
    void isMinimizeApp(MouseEvent event) {
        ((Stage) setScene.getScene().getWindow()).setIconified(true);
    }

    @FXML
    void setSceneStock(MouseEvent event) throws IOException {
        loadScene("stock");
    }

    @FXML
    void setSceneSupplier(MouseEvent event) throws IOException {
        loadScene("supplier");
    }

    @FXML
    void setSceneCategory(MouseEvent event) throws IOException {
        loadScene("category");
    }

    @FXML
    void setSceneCustomer(MouseEvent event) throws IOException {
        loadScene("customer");
    }

    @FXML
    void setSceneTransactions(MouseEvent event) throws IOException {
        loadScene("transactions");
    }

    @FXML
    void setSceneReturn(MouseEvent event) throws IOException {
        loadScene("return");
    }

    @FXML
    void setSceneReport(MouseEvent event) throws IOException {
        loadScene("laporan");
    }

    @FXML
    void setSceneAI(MouseEvent event) throws IOException {
        loadScene("ai");
    }

    @FXML
    void setSceneLogin(MouseEvent event) {
        confirmAction("Konfirmasi", "Apakah anda yakin ingin kembali ke halaman login?", () -> {
            try {
                switchToLoginScene();
            } catch (IOException e) {
                showErrorAlert("Gagal memuat halaman login: " + e.getMessage());
            }
        });
    }

    private void loadScene(String page) throws IOException {
        Redirect.page(page, setScene);
        new FadeIn(setScene).play();
    }

    private void switchToLoginScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/lestarieragemilang/desktop/login.fxml"));
        Parent loginRoot = loader.load();
        Stage stage = (Stage) setScene.getScene().getWindow();
        Scene loginScene = new Scene(loginRoot);

        new FadeOut(setScene).play();
        PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
        delay.setOnFinished(e -> {
            stage.setScene(loginScene);
            new FadeIn(loginRoot).play();
        });
        delay.play();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void confirmAction(String title, String content, Runnable action) {
        Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
        confirmationAlert.setTitle(title);
        confirmationAlert.setHeaderText(null);
        confirmationAlert.setContentText(content);

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            action.run();
        }
    }
}