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
        switchScene(setScene, SceneManager.REPORT_MAIN, () -> {});
    }

    @FXML
    void setSceneReportCategory(MouseEvent event) {
        switchScene(setScene, SceneManager.REPORT_CATEGORY, () -> {});
    }

    @FXML
    void setSceneReportCustomer(MouseEvent event) {
        switchScene(setScene, SceneManager.REPORT_CUSTOMER, () -> {});
    }

    @FXML
    void setSceneReportPurchasing(MouseEvent event) {
        switchScene(setScene, SceneManager.REPORT_PURCHASING, () -> {});
    }

    @FXML
    void setSceneReportSales(MouseEvent event) {
        switchScene(setScene, SceneManager.REPORT_SALES, () -> {});
    }

    @FXML
    void setSceneReportStock(MouseEvent event) {
        switchScene(setScene, SceneManager.REPORT_STOCK, () -> {});
    }

    @FXML
    void setSceneReportSupplier(MouseEvent event) {
        switchScene(setScene, SceneManager.REPORT_SUPPLIER, () -> {});
    }
}
