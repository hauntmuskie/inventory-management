package com.lestarieragemilang.desktop.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Manages JavaFX scenes with caching and transition animations.
 * Provides functionality for loading, caching, and transitioning between
 * scenes.
 * Includes preloading of commonly used scenes for better performance.
 */
public class SceneManager {
    private static final Logger logger = LoggerFactory.getLogger(SceneManager.class);
    private static final String RESOURCE_PATH = "/com/lestarieragemilang/desktop/ui/";
    private static final long CACHE_EXPIRATION_TIME = 30;
    private static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private final Cache<String, Parent> sceneCache;
    private final Set<String> preloadScenes = Set.of(LAYOUT, STOK_BESI, KATEGORI, PELANGGAN);

    public static final String LAYOUT = "layout";
    public static final String STOK_BESI = "stokbesi";
    public static final String KATEGORI = "kategori";
    public static final String PELANGGAN = "pelanggan";
    public static final String PENGEMBALIAN = "pengembalian";
    public static final String SUPPLIER = "supplier";
    public static final String TRANSAKSI = "transaksi";

    public static final String REPORT_MAIN = "laporan";
    public static final String REPORT_STOCK = "laporan-stok";
    public static final String REPORT_CATEGORY = "laporan-kategori";
    public static final String REPORT_CUSTOMER = "laporan-pelanggan";
    public static final String REPORT_PURCHASING = "laporan-pembelian";
    public static final String REPORT_SALES = "laporan-penjualan";
    public static final String REPORT_SUPPLIER = "laporan-supplier";
    public static final String REPORT_RETURN = "laporan-return";

    /**
     * Initializes the SceneManager with a configured cache and preloads common
     * scenes.
     * The cache expires entries after 30 minutes and has a maximum size of 20
     * scenes.
     */
    public SceneManager() {
        this.sceneCache = CacheBuilder.newBuilder()
                .expireAfterWrite(CACHE_EXPIRATION_TIME, TimeUnit.MINUTES)
                .maximumSize(20)
                .build();

        preloadScenes.forEach(scene -> {
            executor.submit(() -> {
                try {
                    loadScene(scene);
                } catch (IOException e) {
                    logger.error("Failed to preload scene: " + scene, e);
                }
            });
        });
    }

    /**
     * Asynchronously loads and returns a scene.
     * 
     * @param sceneName The name of the scene to load
     * @return CompletableFuture containing the loaded Parent node
     */
    public CompletableFuture<Parent> getSceneAsync(String sceneName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getScene(sceneName);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Synchronously loads and returns a scene, using cache when available.
     * 
     * @param sceneName The name of the scene to load
     * @return The loaded Parent node
     * @throws IOException If the FXML file cannot be loaded
     */
    public Parent getScene(String sceneName) throws IOException {
        if (sceneName.startsWith("laporan")) {
            return loadScene(sceneName);
        }

        Parent cachedScene = sceneCache.getIfPresent(sceneName);
        if (cachedScene == null) {
            cachedScene = loadScene(sceneName);
        }
        return cachedScene;
    }

    /**
     * Loads a scene from FXML file and caches it.
     * 
     * @param sceneName The name of the scene to load
     * @return The loaded Parent node
     * @throws IOException If the FXML file cannot be loaded
     */
    private Parent loadScene(String sceneName) throws IOException {
        String resourcePath;
        if (sceneName.startsWith("laporan")) {
            resourcePath = "/com/lestarieragemilang/desktop/ui/report/" + sceneName + ".fxml";
        } else {
            resourcePath = RESOURCE_PATH + sceneName + ".fxml";
        }
        FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
        loader.setClassLoader(this.getClass().getClassLoader());
        Parent root = loader.load();

        Platform.runLater(() -> sceneCache.put(sceneName, root));
        return root;
    }

    /**
     * Removes a specific scene from the cache.
     * 
     * @param sceneName The name of the scene to invalidate
     */
    public void invalidateScene(String sceneName) {
        sceneCache.invalidate(sceneName);
    }

    /**
     * Removes multiple scenes from the cache.
     * 
     * @param sceneNames Array of scene names to invalidate
     */
    public void invalidateScenes(String... sceneNames) {
        sceneCache.invalidateAll(Arrays.asList(sceneNames));
    }

    /**
     * Performs a fade transition between two scenes.
     * 
     * @param currentScene The currently displayed scene
     * @param newScene     The scene to transition to
     * @param onFinished   Callback to execute after the transition completes
     */
    public void transitionTo(Parent currentScene, Parent newScene, Runnable onFinished) {
        if (currentScene == null || newScene == null) {
            if (onFinished != null)
                onFinished.run();
            return;
        }

        if (newScene.getScene() != null) {
            ThemeManager.getInstance().applyTheme(newScene.getScene());
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), currentScene);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), newScene);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        fadeOut.setOnFinished(_ -> {
            if (onFinished != null)
                onFinished.run();
            fadeIn.play();
        });

        fadeOut.play();
    }
}