package com.lestarieragemilang.desktop.utils;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import javafx.scene.Scene;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import java.util.prefs.Preferences;

/**
 * Manages the application's theme using JMetro styling.
 * Supports theme switching and persistence between application launches.
 */
public class ThemeManager {
    private static final String THEME_PREF_KEY = "app_theme";
    private static final String EVENT_BUS_NAME = "ThemeEventBus";
    private static final String NULL_SCENE_MESSAGE = "Scene cannot be null";
    private static final String NULL_LISTENER_MESSAGE = "Listener cannot be null";
    private static final String NULL_STYLE_MESSAGE = "Style cannot be null";
    private static final Style DEFAULT_STYLE = Style.LIGHT;

    private static volatile ThemeManager instance;
    private final Preferences prefs;
    private JMetro jMetro;
    private final EventBus eventBus;

    private ThemeManager() {
        prefs = Preferences.userNodeForPackage(ThemeManager.class);
        eventBus = new EventBus(EVENT_BUS_NAME);

        String savedStyleString = prefs.get(THEME_PREF_KEY, DEFAULT_STYLE.toString());
        Style savedStyle = savedStyleString.equals(DEFAULT_STYLE.toString())
                ? DEFAULT_STYLE
                : Style.DARK;
        jMetro = new JMetro(savedStyle);
    }

    public static ThemeManager getInstance() {
        if (instance == null) {
            synchronized (ThemeManager.class) {
                if (instance == null) {
                    instance = new ThemeManager();
                }
            }
        }
        return instance;
    }

    /**
     * Applies the current theme to a JavaFX scene.
     * 
     * @param scene The JavaFX scene to apply the theme to
     */
    public void applyTheme(Scene scene) {
        Preconditions.checkNotNull(scene, NULL_SCENE_MESSAGE);
        jMetro.setScene(scene);
    }

    /**
     * Switches between light and dark theme.
     * 
     * @param style The style to switch to
     */
    public void switchTheme(Style style) {
        Preconditions.checkNotNull(style, NULL_STYLE_MESSAGE);
        jMetro = new JMetro(style);
        prefs.put(THEME_PREF_KEY, style.toString());
        eventBus.post(new ThemeChangeEvent(style));
    }

    /**
     * @return The current theme style
     */
    public Style getCurrentTheme() {
        return jMetro.getStyle();
    }

    /**
     * Registers a listener that will be notified when theme changes
     * 
     * @param listener The listener to register
     */
    public void registerThemeListener(Object listener) {
        Preconditions.checkNotNull(listener, NULL_LISTENER_MESSAGE);
        eventBus.register(listener);
    }

    /**
     * Unregisters a listener from theme change notifications
     * 
     * @param listener The listener to unregister
     */
    public void unregisterThemeListener(Object listener) {
        Preconditions.checkNotNull(listener, NULL_LISTENER_MESSAGE);
        eventBus.unregister(listener);
    }

    public static class ThemeChangeEvent {
        private final Style style;

        public ThemeChangeEvent(Style style) {
            this.style = style;
        }

        public Style getStyle() {
            return style;
        }
    }
}
