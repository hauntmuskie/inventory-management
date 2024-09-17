package com.lestarieragemilang.desktop.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.concurrent.Task;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SceneManager {
    private static final String RESOURCE_PATH = "/com/lestarieragemilang/desktop/";
    private static final long CACHE_EXPIRATION_TIME = 30;
    private final Cache<String, Parent> sceneCache;

    private static final List<String> SCENE_NAMES = Arrays.asList(
            "layout", "stokbesi", "kategori", "pelanggan", "pengembalian", "stokbesi", "supplier", "transaksi");

    public SceneManager() {
        this.sceneCache = CacheBuilder.newBuilder()
                .expireAfterWrite(CACHE_EXPIRATION_TIME, TimeUnit.MINUTES)
                .build();
    }

    public void preloadScenes() {
        Task<Void> preloadTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                for (String sceneName : SCENE_NAMES) {
                    try {
                        loadScene(sceneName);
                    } catch (IOException e) {
                        System.err.println("Failed to preload scene: " + sceneName);
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };

        new Thread(preloadTask).start();
    }

    public Parent getScene(String sceneName) throws IOException {
        Parent cachedScene = sceneCache.getIfPresent(sceneName);
        if (cachedScene == null) {
            cachedScene = loadScene(sceneName);
        }
        return cachedScene;
    }

    private Parent loadScene(String sceneName) throws IOException {
        String resourcePath = RESOURCE_PATH + sceneName + ".fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
        Parent root = loader.load();
        sceneCache.put(sceneName, root);
        return root;
    }
}