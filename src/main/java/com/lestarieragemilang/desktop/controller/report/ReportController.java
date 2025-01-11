package com.lestarieragemilang.desktop.controller.report;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import com.lestarieragemilang.desktop.App;
import com.lestarieragemilang.desktop.utils.Redirect;
import com.lestarieragemilang.desktop.utils.SceneManager;
import com.lestarieragemilang.desktop.utils.ShowAlert;

public class ReportController extends Redirect {

    @FXML
    private BorderPane bp;

    @FXML
    private AnchorPane setScene;

    private String currentScene = SceneManager.REPORT_MAIN;

    private void loadReportScene(String sceneName) {
        if (currentScene.equals(sceneName)) {
            return;
        }

        App.sceneManager.getSceneAsync(sceneName)
                .thenAcceptAsync(newScene -> {
                    if (setScene.getChildren().isEmpty()) {
                        setScene.getChildren().setAll(newScene);
                        animateFadeIn(newScene);
                    } else {
                        Parent currentSceneNode = (Parent) setScene.getChildren().get(0);
                        App.sceneManager.transitionTo(currentSceneNode, newScene, () -> {
                            setScene.getChildren().setAll(newScene);
                            currentScene = sceneName;
                        });
                    }
                }, Platform::runLater)
                .exceptionally(e -> {
                    ShowAlert.showError("Gagal memuat laporan: " + sceneName);
                    e.printStackTrace();
                    return null;
                });
    }

    @FXML
    void backToReportMain(MouseEvent event) {
        if (!currentScene.equals(SceneManager.REPORT_MAIN)) {
            loadReportScene(SceneManager.REPORT_MAIN);
        }
    }

    @FXML
    void setSceneReportCategory(MouseEvent event) {
        loadReportScene(SceneManager.REPORT_CATEGORY);
    }

    @FXML
    void setSceneReportCustomer(MouseEvent event) {
        loadReportScene(SceneManager.REPORT_CUSTOMER);
    }

    @FXML
    void setSceneReportPurchasing(MouseEvent event) {
        loadReportScene(SceneManager.REPORT_PURCHASING);
    }

    @FXML
    void setSceneReportSales(MouseEvent event) {
        loadReportScene(SceneManager.REPORT_SALES);
    }

    @FXML
    void setSceneReportStock(MouseEvent event) {
        loadReportScene(SceneManager.REPORT_STOCK);
    }

    @FXML
    void setSceneReportSupplier(MouseEvent event) {
        loadReportScene(SceneManager.REPORT_SUPPLIER);
    }

    @FXML
    void setSceneReportReturn(MouseEvent event) {
        loadReportScene(SceneManager.REPORT_RETURN);
    }

    @Override
    protected void animateFadeIn(Parent node) {
        node.setOpacity(0);
        new animatefx.animation.FadeIn(node).setSpeed(2.0).play();
    }

    @Override
    protected void animateFadeOut(Parent node, Runnable onFinished) {
        animatefx.animation.FadeOut fadeOut = new animatefx.animation.FadeOut(node);
        fadeOut.setSpeed(2.0);
        fadeOut.setOnFinished(_ -> onFinished.run());
        fadeOut.play();
    }
}
