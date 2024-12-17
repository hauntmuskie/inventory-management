package com.lestarieragemilang.desktop.utils;

import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;

import java.io.IOException;

import com.lestarieragemilang.desktop.App;

public abstract class Redirect {

  protected abstract void animateFadeIn(Parent node);

  protected abstract void animateFadeOut(Parent node, Runnable onFinished);

  protected Parent loadScene(String page, AnchorPane anchorPane) throws IOException {
    if (!Platform.isFxApplicationThread()) {
      // If we're not on the FX thread, submit the work to it
      Platform.runLater(() -> {
        try {
          loadScene(page, anchorPane);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
      return anchorPane;
    }
    
    Parent root = App.sceneManager.getScene(page);
    anchorPane.getChildren().setAll(root);
    animateFadeIn(anchorPane);
    return root;
  }

  protected void switchScene(AnchorPane currentScene, String newSceneName, Runnable setNewScene) {
    animateFadeOut(currentScene, () -> {
      PauseTransition delay = new PauseTransition(Duration.seconds(0.5));
      delay.setOnFinished(_ -> {
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
}