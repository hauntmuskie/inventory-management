package com.lestarieragemilang.desktop.controller.report;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import com.lestarieragemilang.desktop.utils.Redirect;
import com.lestarieragemilang.desktop.utils.SceneManager;

public class ReportController extends Redirect {

    @FXML
    private BorderPane bp;

    @FXML
    private AnchorPane setScene;

    private String currentScene = SceneManager.REPORT_MAIN;

    @Override
    protected void animateFadeIn(Parent node) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    @Override
    protected void animateFadeOut(Parent node, Runnable onFinished) {
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), node);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(_ -> onFinished.run());
        fadeOut.play();
    }

    @FXML
    void backToReportMain(MouseEvent event) {
        if (!currentScene.equals(SceneManager.REPORT_MAIN)) {
            switchScene(setScene, SceneManager.REPORT_MAIN, () -> {
                currentScene = SceneManager.REPORT_MAIN;
                
                setScene.getChildren().clear();
            });
        }
    }

    @FXML
    void setSceneReportCategory(MouseEvent event) {
        if (!currentScene.equals(SceneManager.REPORT_CATEGORY)) {
            switchScene(setScene, SceneManager.REPORT_CATEGORY, () -> {
                currentScene = SceneManager.REPORT_CATEGORY;
            });
        }
    }

    @FXML
    void setSceneReportCustomer(MouseEvent event) {
        if (!currentScene.equals(SceneManager.REPORT_CUSTOMER)) {
            switchScene(setScene, SceneManager.REPORT_CUSTOMER, () -> {
                currentScene = SceneManager.REPORT_CUSTOMER;
            });
        }
    }

    @FXML
    void setSceneReportPurchasing(MouseEvent event) {
        if (!currentScene.equals(SceneManager.REPORT_PURCHASING)) {
            switchScene(setScene, SceneManager.REPORT_PURCHASING, () -> {
                currentScene = SceneManager.REPORT_PURCHASING;
            });
        }
    }

    @FXML
    void setSceneReportSales(MouseEvent event) {
        if (!currentScene.equals(SceneManager.REPORT_SALES)) {
            switchScene(setScene, SceneManager.REPORT_SALES, () -> {
                currentScene = SceneManager.REPORT_SALES;
            });
        }
    }

    @FXML
    void setSceneReportStock(MouseEvent event) {
        if (!currentScene.equals(SceneManager.REPORT_STOCK)) {
            switchScene(setScene, SceneManager.REPORT_STOCK, () -> {
                currentScene = SceneManager.REPORT_STOCK;
            });
        }
    }

    @FXML
    void setSceneReportSupplier(MouseEvent event) {
        if (!currentScene.equals(SceneManager.REPORT_SUPPLIER)) {
            switchScene(setScene, SceneManager.REPORT_SUPPLIER, () -> {
                currentScene = SceneManager.REPORT_SUPPLIER;
            });
        }
    }

    @FXML
    void setSceneReportReturn(MouseEvent event) {
        if (!currentScene.equals(SceneManager.REPORT_RETURN)) {
            switchScene(setScene, SceneManager.REPORT_RETURN, () -> {
                currentScene = SceneManager.REPORT_RETURN;
            });
        }
    }
}
