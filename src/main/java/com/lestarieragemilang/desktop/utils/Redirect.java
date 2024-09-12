package com.lestarieragemilang.desktop.utils;

import java.io.IOException;
import java.net.URL;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.AnchorPane;

public class Redirect {
  public static void page(String page, AnchorPane anchorPane) throws IOException {
    if (anchorPane == null) {
      throw new IllegalArgumentException("anchorPane cannot be null");
    }

    String path = "/com/lestarieragemilang/desktop/" + page + ".fxml";

    URL resource = Redirect.class.getResource(path);

    if (resource == null) {
      throw new IOException("Resource not found: " + path);
    }

    FXMLLoader loader = new FXMLLoader(resource);
    Parent root = loader.load();

    anchorPane.getChildren().setAll(root);
  }

}