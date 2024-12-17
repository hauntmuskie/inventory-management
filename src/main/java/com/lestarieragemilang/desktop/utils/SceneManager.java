package com.lestarieragemilang.desktop.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.concurrent.Task;
import javafx.application.Platform;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SceneManager {
    private static final String RESOURCE_PATH = "/com/lestarieragemilang/desktop/";
    private static final long CACHE_EXPIRATION_TIME = 30;
    private final Cache<String, Parent> sceneCache;

    // Add constants for scene names
    public static final String LAYOUT = "layout";
    public static final String STOK_BESI = "stokbesi";
    public static final String KATEGORI = "kategori";
    public static final String PELANGGAN = "pelanggan";
    public static final String PENGEMBALIAN = "pengembalian";
    public static final String SUPPLIER = "supplier";
    public static final String TRANSAKSI = "transaksi";

    // Add constants for report scenes
    public static final String REPORT_MAIN = "laporan";
    public static final String REPORT_STOCK = "laporan-stok";
    public static final String REPORT_CATEGORY = "laporan-kategori";
    public static final String REPORT_CUSTOMER = "laporan-pelanggan";
    public static final String REPORT_PURCHASING = "laporan-pembelian";
    public static final String REPORT_SALES = "laporan-penjualan";
    public static final String REPORT_SUPPLIER = "laporan-supplier";

    private static final List<String> SCENE_NAMES = Arrays.asList(
            LAYOUT, STOK_BESI, KATEGORI, PELANGGAN, PENGEMBALIAN, SUPPLIER, TRANSAKSI,
            REPORT_MAIN, REPORT_STOCK, REPORT_CATEGORY, REPORT_CUSTOMER, 
            REPORT_PURCHASING, REPORT_SALES, REPORT_SUPPLIER);

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
                        String resourcePath = RESOURCE_PATH + sceneName + ".fxml";
                        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
                        // Load on background thread
                        Parent root = loader.load();
                        // Update cache on FX thread
                        Platform.runLater(() -> sceneCache.put(sceneName, root));
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

    public void invalidateScene(String sceneName) {
        sceneCache.invalidate(sceneName);
    }

    public void invalidateScenes(String... sceneNames) {
        sceneCache.invalidateAll(Arrays.asList(sceneNames));
    }
}