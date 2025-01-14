package com.lestarieragemilang.desktop.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;

import com.lestarieragemilang.desktop.App;

/**
 * Abstract class that handles scene transitions and animations in JavaFX applications.
 * Provides base functionality for loading and switching between scenes with fade animations.
 */
public abstract class Redirect {

  /**
   * Implements fade-in animation for a scene transition.
   * @param node The JavaFX Parent node to animate
   */
  protected abstract void animateFadeIn(Parent node);

  /**
   * Implements fade-out animation for a scene transition.
   * @param node The JavaFX Parent node to animate
   * @param onFinished Callback to execute after animation completes
   */
  protected abstract void animateFadeOut(Parent node, Runnable onFinished);

  /**
   * Loads a new scene into the specified AnchorPane with fade-in animation.
   * If called from a non-JavaFX thread, the operation is delegated to the JavaFX thread.
   * 
   * @param page The name/path of the scene to load
   * @param anchorPane The AnchorPane container for the new scene
   * @return The loaded Parent node
   * @throws IOException If scene loading fails
   * @throws IllegalArgumentException If page is null/empty or anchorPane is null
   */
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

  /**
   * Switches from current scene to a new scene with fade-out animation.
   * Hides the current stage after scene transition.
   * 
   * @param currentScene The current AnchorPane being displayed
   * @param newSceneName Name of the new scene to switch to
   * @param setNewScene Runnable that sets up the new scene
   * @throws IllegalArgumentException If any parameter is null or newSceneName is empty
   */
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