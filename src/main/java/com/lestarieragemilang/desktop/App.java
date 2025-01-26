package com.lestarieragemilang.desktop;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.lestarieragemilang.desktop.utils.SceneManager;
import com.lestarieragemilang.desktop.utils.ShowAlert;
import com.lestarieragemilang.desktop.utils.ThemeManager;
import com.lestarieragemilang.desktop.utils.HibernateUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import animatefx.animation.FadeIn;

/**
 * Main application class that initializes and manages the JavaFX UI
 * application.
 * Handles scene transitions, database connectivity checks, and theme
 * management.
 */
public class App extends Application {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    /** Initial scene to be displayed when application starts */
    private static final String INITIAL_SCENE = "login";

    /** Default window dimensions */
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 650;

    /** Main application scene and managers */
    private static Scene scene;
    public static SceneManager sceneManager;
    private static final ThemeManager themeManager = ThemeManager.getInstance();

    /** Flag to enable/disable JMetro theming */
    private static final boolean USE_JMETRO = true;

    /** Set of scene identifiers that are related to reporting functionality */
    private static final ImmutableSet<String> REPORT_SCENES = ImmutableSet.of(
            SceneManager.REPORT_STOCK,
            SceneManager.REPORT_CATEGORY,
            SceneManager.REPORT_CUSTOMER,
            SceneManager.REPORT_PURCHASING,
            SceneManager.REPORT_SALES,
            SceneManager.REPORT_SUPPLIER);

    /**
     * Application entry point. Initializes the primary stage and handles any
     * startup errors.
     * 
     * @param stage The primary stage for the application
     */
    @Override
    public void start(Stage stage) {
        try {
            initializeApplication(stage);
        } catch (Exception e) {
            handleError("Terjadi kesalahan yang tidak terduga", e);
        }
    }

    /**
     * Initializes core application components including scene manager, database
     * check,
     * and primary window setup.
     * 
     * @param stage The primary stage to initialize
     * @throws IOException If scene loading fails
     */
    private void initializeApplication(Stage stage) throws IOException {
        sceneManager = Preconditions.checkNotNull(new SceneManager(), "Scene manager cannot be null");
        checkDatabase();

        var root = sceneManager.getScene(INITIAL_SCENE);
        scene = new Scene(root, WIDTH, HEIGHT);

        if (USE_JMETRO) {
            themeManager.applyTheme(scene);
        }

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.centerOnScreen();

        new FadeIn(root).play();
        stage.show();
    }

    /**
     * Verifies database connectivity and shows warning if database is unavailable.
     * Limited functionality will be available without database access.
     */
    private void checkDatabase() {
        if (!HibernateUtil.isDatabaseAvailable()) {
            ShowAlert.showWarning(
                    "Database tidak tersedia. Aplikasi akan berjalan dengan fitur terbatas tanpa akses database.\n" +
                            "Pastikan XAMPP sudah berjalan untuk fungsionalitas penuh.");
        }
    }

    /**
     * Handles application errors by logging them and showing user-friendly error
     * messages.
     * 
     * @param message User-friendly error message
     * @param e       Exception that occurred
     */
    private void handleError(String message, Exception e) {
        log.error(message + ": " + Throwables.getStackTraceAsString(e));
        ShowAlert.showError(message + "\n" + Throwables.getRootCause(e).getMessage());
    }

    /**
     * Changes the current scene to a new one specified by the FXML file name.
     * Handles scene transitions asynchronously with animations.
     * 
     * @param fxml The name of the FXML file to load
     */
    public static void setRoot(String fxml) {
        if (SceneManager.REPORT_MAIN.equals(fxml)) {
            invalidateReportScenes();
        }

        sceneManager.getSceneAsync(fxml)
                .thenAcceptAsync(newRoot -> {
                    if (newRoot != null) {
                        Parent currentRoot = scene.getRoot();

                        themeManager.applyTheme(scene);
                        sceneManager.transitionTo(currentRoot, newRoot, () -> scene.setRoot(newRoot));
                    }
                }, Platform::runLater)
                .exceptionally(e -> {
                    log.error("Failed to set root for scene: {}", fxml, e);
                    ShowAlert.showError("Failed to load page: " + fxml);
                    return null;
                });
    }

    /**
     * Invalidates cached report scenes to ensure fresh data loading.
     * Called when returning to the main report view.
     */
    private static void invalidateReportScenes() {
        sceneManager.invalidateScenes(REPORT_SCENES.toArray(new String[0]));
    }

    /**
     * Application main entry point.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch();
    }
}