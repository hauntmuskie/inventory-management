package com.lestarieragemilang.desktop.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.control.Alert.AlertType;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public abstract class Redirect {
  private static final String RESOURCE_PATH = "/com/lestarieragemilang/desktop/";
  protected final LoadingCache<String, Parent> sceneCache = CacheBuilder.newBuilder()
      .expireAfterAccess(10, TimeUnit.MINUTES)
      .build(new CacheLoader<String, Parent>() {

        @SuppressWarnings("null")
        @Override
        public Parent load(String page) throws Exception {
          return loadFXML(page);
        }
      });

  protected abstract void animateFadeIn(Parent node);

  protected abstract void animateFadeOut(Parent node, Runnable onFinished);

  protected Parent loadScene(String page, AnchorPane anchorPane) throws IOException {
    Preconditions.checkNotNull(anchorPane, "anchorPane cannot be null");

    try {
      Parent root = sceneCache.get(page);
      anchorPane.getChildren().setAll(root);
      animateFadeIn(anchorPane);
      return root;
    } catch (ExecutionException e) {
      throw new IOException("Failed to load scene: " + page, e);
    }
  }

  private Parent loadFXML(String page) throws IOException {
    String path = RESOURCE_PATH + page + ".fxml";
    URL resource = getClass().getResource(path);
    if (resource == null) {
      throw new IOException("Resource not found: " + path);
    }
    return FXMLLoader.load(resource);
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
}