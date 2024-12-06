package com.lestarieragemilang.desktop;

import com.lestarieragemilang.desktop.utils.SceneManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    private static final String INITIAL_SCENE = "login";
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 650;
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    @SuppressWarnings("exports")
    public static SceneManager sceneManager;

    @Override
    public void start(Stage stage) {
        try {
            sceneManager = new SceneManager();

            Parent root = sceneManager.getScene(INITIAL_SCENE);

            scene = new Scene(root, WIDTH, HEIGHT);

            JMetro jMetro = new JMetro(Style.LIGHT);
            jMetro.setScene(scene);

            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            stage.centerOnScreen();

            new FadeIn(root).play();

            stage.show();

            // Preload scenes in the background
            sceneManager.preloadScenes();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load the initial scene: " + INITIAL_SCENE, e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An unexpected error occurred", e);
        }
    }

    public static void setRoot(String fxml) throws IOException {
        Parent newRoot = sceneManager.getScene(fxml);
        new FadeIn(newRoot).play();
        scene.setRoot(newRoot);
    }

    public static void main(String[] args) {
        launch();
    }
}