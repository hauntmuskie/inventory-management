package com.lestarieragemilang.desktop;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.lestarieragemilang.desktop.utils.SceneManager;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import animatefx.animation.FadeIn;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final String INITIAL_SCENE = "layout";
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 650;
    private static Scene scene;
    @SuppressWarnings("exports")
    public static SceneManager sceneManager;

    private static final ImmutableSet<String> REPORT_SCENES = ImmutableSet.of(
        SceneManager.REPORT_STOCK,
        SceneManager.REPORT_CATEGORY,
        SceneManager.REPORT_CUSTOMER,
        SceneManager.REPORT_PURCHASING,
        SceneManager.REPORT_SALES,
        SceneManager.REPORT_SUPPLIER
    );

    @Override
    public void start(Stage stage) {
        try {
            initializeApplication(stage);
        } catch (Exception e) {
            handleError("Terjadi kesalahan yang tidak terduga", e);
        }
    }

    private void initializeApplication(Stage stage) throws IOException {
        sceneManager = Preconditions.checkNotNull(new SceneManager(), "Scene manager cannot be null");
        checkDatabase();
        
        var root = sceneManager.getScene(INITIAL_SCENE);
        scene = new Scene(root, WIDTH, HEIGHT);
        
        var jMetro = new JMetro(Style.LIGHT);
        jMetro.setScene(scene);

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.centerOnScreen();

        new FadeIn(root).play();
        stage.show();
    }

    private void checkDatabase() {
        if (!HibernateUtil.isDatabaseAvailable()) {
            ShowAlert.showWarning(
                "Database tidak tersedia. Aplikasi akan berjalan dengan fitur terbatas tanpa akses database.\n" +
                "Pastikan XAMPP sudah berjalan untuk fungsionalitas penuh."
            );
        }
    }

    private void handleError(String message, Exception e) {
        log.error(message + ": " + Throwables.getStackTraceAsString(e));
        ShowAlert.showError(message + "\n" + Throwables.getRootCause(e).getMessage());
    }

    public static void setRoot(String fxml) {
        try {
            Preconditions.checkNotNull(fxml, "FXML name cannot be null");
            
            if (SceneManager.REPORT_MAIN.equals(fxml)) {
                invalidateReportScenes();
            }

            Parent newRoot = sceneManager.getScene(fxml);
            if (newRoot != null) {
                Parent currentRoot = scene.getRoot();
                sceneManager.transitionTo(currentRoot, newRoot, () -> scene.setRoot(newRoot));
            }
        } catch (IOException e) {
            log.error("Failed to set root for scene: {}", fxml, e);
            ShowAlert.showError("Gagal memuat halaman: " + fxml + "\n" + Throwables.getRootCause(e).getMessage());
        }
    }

    private static void invalidateReportScenes() {
        sceneManager.invalidateScenes(REPORT_SCENES.toArray(new String[0]));
    }

    public static void main(String[] args) {
        launch();
    }
}