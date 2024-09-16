package com.lestarieragemilang.desktop.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public abstract class Redirect {
  private static final String RESOURCE_PATH = "/com/lestarieragemilang/desktop/";
  protected final Map<String, Parent> sceneCache = new HashMap<>();

  protected abstract void animateFadeIn(Parent node);

  protected abstract void animateFadeOut(Parent node, Runnable onFinished);

  protected void loadScene(String page, AnchorPane anchorPane) throws IOException {
    if (anchorPane == null) {
      throw new IllegalArgumentException("anchorPane cannot be null");
    }

    Parent root = sceneCache.computeIfAbsent(page, this::loadFXML);
    anchorPane.getChildren().setAll(root);
    animateFadeIn(anchorPane);
  }

  private Parent loadFXML(String page) {
    try {
      String path = RESOURCE_PATH + page + ".fxml";
      URL resource = getClass().getResource(path);
      if (resource == null) {
        throw new IOException("Resource not found: " + path);
      }
      return FXMLLoader.load(resource);
    } catch (IOException e) {
      ShowAlert.showAlert(AlertType.ERROR, "Error", "FXML Loading Error", "Failed to load FXML: " + page);
      throw new RuntimeException("Failed to load FXML: " + page, e);
    }
  }

  protected void switchScene(AnchorPane currentScene, String newSceneName, Runnable setNewScene) {
    animateFadeOut(currentScene, () -> {
      PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
      delay.setOnFinished(e -> {
        setNewScene.run();
        try {
          loadScene(newSceneName, currentScene);
        } catch (IOException ex) {
          ShowAlert.showAlert(AlertType.ERROR, "Error", "Scene Switch Error", "Failed to switch scene");
          throw new RuntimeException("Failed to switch scene", ex);
        }
      });
      delay.play();
    });
  }

  protected void showAlert(AlertType alertType, String title, String headerText, String... messages) {
    ShowAlert.showAlert(alertType, title, headerText, messages);
  }

  protected boolean showConfirmation(String title, String headerText, String content) {
    return ShowAlert.showConfirmation(title, headerText, content);
  }
}