package com.lestarieragemilang.desktop;

import com.lestarieragemilang.desktop.utils.SceneManager;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import animatefx.animation.FadeIn;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class App extends Application {

    private static Scene scene;
    private static final String INITIAL_SCENE = "layout";
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 650;
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    @SuppressWarnings("exports")
    public static SceneManager sceneManager;

    @Override
    public void start(Stage stage) {
        try {
            sceneManager = new SceneManager();

            // Check database availability and show warning if needed
            if (!HibernateUtil.isDatabaseAvailable()) {
                Alert alert = new Alert(AlertType.WARNING);
                alert.setTitle("Database Unavailable");
                alert.setHeaderText("Database Connection Failed");
                alert.setContentText("The application will run in limited mode without database access. Please ensure XAMPP is running for full functionality.");
                alert.show();
            }

            Parent root = sceneManager.getScene(INITIAL_SCENE);

            scene = new Scene(root, WIDTH, HEIGHT);

            JMetro jMetro = new JMetro(Style.LIGHT);
            jMetro.setScene(scene);

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            stage.centerOnScreen();

            new FadeIn(root).play();

            stage.show();
            
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load the initial scene: " + INITIAL_SCENE, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred", e);
        }
    }

    public static void setRoot(String fxml) throws IOException {
        // Invalidate report scenes when navigating back to main laporan
        if (fxml.equals(SceneManager.REPORT_MAIN)) {
            sceneManager.invalidateScenes(
                SceneManager.REPORT_STOCK,
                SceneManager.REPORT_CATEGORY,
                SceneManager.REPORT_CUSTOMER,
                SceneManager.REPORT_PURCHASING,
                SceneManager.REPORT_SALES,
                SceneManager.REPORT_SUPPLIER
            );
        }
        Parent newRoot = sceneManager.getScene(fxml);
        new FadeIn(newRoot).play();
        scene.setRoot(newRoot);
    }

    public static void main(String[] args) {
        launch();
    }
}