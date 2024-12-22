package com.lestarieragemilang.desktop.controller;

import com.jfoenix.controls.JFXButton;
import com.lestarieragemilang.desktop.App;
import com.lestarieragemilang.desktop.utils.Redirect;
import com.lestarieragemilang.desktop.utils.ShowAlert;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Layout extends Redirect {

    @FXML
    private AnchorPane setScene;
    @FXML
    private Label localTimeApp;
    @FXML
    private JFXButton navButtons;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy '['HH:mm:ss']'",
            new Locale.Builder().setLanguageTag("id").build());

    private JFXButton currentActiveButton;

    @FXML
    public void initialize() throws IOException {
        loadScene("StokBesi", setScene);
        initializeClock();
    }

    private void initializeClock() {
        Timeline clock = new Timeline(
                new KeyFrame(Duration.ZERO, _ -> updateClock()),
                new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void updateClock() {
        LocalDateTime currentTime = LocalDateTime.now();
        localTimeApp.setText(currentTime.format(FORMATTER));
    }

    private void setActiveButton(JFXButton button) {
        if (button == null) {
            System.err.println("Warning: Attempted to set null button as active");
            return;
        }
        if (currentActiveButton != null) {
            currentActiveButton
                    .setStyle(
                            "-fx-background-color: #EEEEEE; -fx-background-radius: 10; -fx-text-fill: #333333; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-radius: 10;");
        }
        button.setStyle(
                "-fx-background-color: #131313; -fx-background-radius: 10; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-radius: 10;");
        currentActiveButton = button;
    }

    @FXML
    void isExitApp(MouseEvent event) {
        if (ShowAlert.showConfirmation("Exit", "Konfirmasi", "Apakah Anda yakin ingin keluar?")) {
            System.exit(0);
        }
    }

    @FXML
    void isMinimizeApp(MouseEvent event) {
        getStage().setIconified(true);
    }

    @FXML
    void handleNavButtonClick(MouseEvent event) throws IOException {
        if (event.getSource() instanceof JFXButton) {
            JFXButton clickedButton = (JFXButton) event.getSource();
            String buttonText = clickedButton.getText();

            String sceneName = buttonText.replaceAll("\\s+", "");

            System.out.println("Button clicked: " + buttonText);
            System.out.println("Loading scene: " + sceneName);

            loadScene(sceneName.toLowerCase(), setScene);
            setActiveButton(clickedButton);
        } else {
            System.err.println("Error: Event source is not a JFXButton");
        }
    }

    @FXML
    void setSceneLogin(MouseEvent event) {
        if (ShowAlert.showConfirmation("Konfirmasi", "Login", "Apakah anda yakin ingin kembali ke halaman login?")) {
            Stage currentStage = getStage();
            switchToLoginScene();
            currentStage.close();
        }
    }

    private void switchToLoginScene() {
        try {
            Stage loginStage = new Stage();
            Parent root = App.sceneManager.getScene("login");
            Scene scene = new Scene(root);
            loginStage.setScene(scene);
            loginStage.show();
        } catch (IOException e) {
            ShowAlert.showError("Gagal membuka halaman login");
            throw new RuntimeException("Gagal membuka halaman login", e);
        }
    }

    @Override
    protected void animateFadeIn(Parent node) {
        new animatefx.animation.FadeIn(node).play();
    }

    @Override
    protected void animateFadeOut(Parent node, Runnable onFinished) {
        animatefx.animation.FadeOut fadeOut = new animatefx.animation.FadeOut(node);
        fadeOut.setOnFinished(_ -> onFinished.run());
        fadeOut.play();
    }

    private Stage getStage() {
        return (Stage) setScene.getScene().getWindow();
    }
}