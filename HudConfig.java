package com.pojavhud;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HudConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("pojavhud.json");

    public IconConfig[] icons = new IconConfig[9];

    public HudConfig() {
        float startX = 0.35f;
        float spacing = 0.04f;
        for (int i = 0; i < 9; i++) {
            icons[i] = new IconConfig();
            icons[i].x = startX + i * spacing;
            icons[i].y = 0.5f;
            icons[i].scale = 1.0f;
            icons[i].opacity = 1.0f;
        }
    }

    public static class IconConfig {
        public float x = 0.5f;
        public float y = 0.5f;
        public float scale = 1.0f;
        public float opacity = 1.0f;
    }

    public static HudConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                HudConfig config = GSON.fromJson(json, HudConfig.class);
                if (config != null && config.icons != null && config.icons.length == 9) {
                    return config;
                }
            } catch (Exception e) {
                PojavHudClient.LOGGER.error("Failed to load config", e);
            }
        }
        HudConfig config = new HudConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            PojavHudClient.LOGGER.error("Failed to save config", e);
        }
    }

    public void reset() {
        HudConfig defaults = new HudConfig();
        this.icons = defaults.icons;
        save();
    }
}
