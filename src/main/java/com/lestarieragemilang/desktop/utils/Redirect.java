package com.lestarieragemilang.desktop.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;

import com.lestarieragemilang.desktop.App;

public abstract class Redirect {

  protected abstract void animateFadeIn(Parent node);

  protected abstract void animateFadeOut(Parent node, Runnable onFinished);

  protected Parent loadScene(String page, AnchorPane anchorPane) throws IOException {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(page), "Nama halaman tidak boleh kosong");
    Preconditions.checkNotNull(anchorPane, "AnchorPane tidak boleh kosong");

    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(() -> {
        try {
          loadScene(page, anchorPane);
        } catch (IOException e) {
          throw new RuntimeException("Gagal memuat halaman", e);
        }
      });
      return anchorPane;
    }
    
    Parent root = Preconditions.checkNotNull(
        App.sceneManager.getScene(page),
        "Scene %s tidak ditemukan", page
    );
    anchorPane.getChildren().setAll(root);
    animateFadeIn(anchorPane);
    return root;
  }

  protected void switchScene(AnchorPane currentScene, String newSceneName, Runnable setNewScene) {
    Preconditions.checkNotNull(currentScene, "Scene saat ini tidak boleh kosong");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(newSceneName), "Nama scene baru tidak boleh kosong");
    Preconditions.checkNotNull(setNewScene, "SetNewScene tidak boleh kosong");

    animateFadeOut(currentScene, () -> {
      Platform.runLater(() -> {
        setNewScene.run();
        try {
          Stage currentStage = (Stage) currentScene.getScene().getWindow();
          if (currentStage != null) {
            currentStage.hide();
          }
        } catch (Exception e) {
            System.err.println("Error hiding current stage: " + e.getMessage());
        }
      });
    });
  }
}