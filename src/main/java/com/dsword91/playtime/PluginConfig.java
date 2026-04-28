package com.dsword91.playtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PluginConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = CONFIG_DIR + "/playtime_config.json";

    private int apiPort = 25002;
    private int afkThresholdTicks = 100;
    private double movementThreshold = 0.1;
    private long saveInterval = 6000;
    private String apiKey = "";

    private static PluginConfig instance;
    private PluginConfig() {}

    public static PluginConfig getInstance() {
        if (instance == null) {
            instance = new PluginConfig();
            instance.loadConfig();
        }
        return instance;
    }

    public void loadConfig() {
        Path configPath = Path.of(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            LOGGER.info("未找到配置文件，使用默认配置");
            saveConfig();
            return;
        }
        try {
            String json = Files.readString(configPath);
            PluginConfig loaded = GSON.fromJson(json, PluginConfig.class);
            if (loaded != null) {
                this.apiPort = loaded.apiPort;
                this.afkThresholdTicks = loaded.afkThresholdTicks;
                this.movementThreshold = loaded.movementThreshold;
                this.saveInterval = loaded.saveInterval;
                this.apiKey = loaded.apiKey;
                LOGGER.info("已加载配置文件: {}", CONFIG_FILE);
            }
        } catch (IOException e) {
            LOGGER.error("加载配置文件失败，使用默认配置", e);
        }
    }

    public void saveConfig() {
        try {
            Path configDir = Path.of(CONFIG_DIR);
            if (!Files.exists(configDir)) Files.createDirectories(configDir);
            Path configPath = Path.of(CONFIG_FILE);
            String json = GSON.toJson(this);
            Files.writeString(configPath, json);
            LOGGER.info("已保存配置文件到 {}", CONFIG_FILE);
        } catch (IOException e) {
            LOGGER.error("保存配置文件失败", e);
        }
    }

    public int getApiPort() { return apiPort; }
    public int getAfkThresholdTicks() { return afkThresholdTicks; }
    public double getMovementThreshold() { return movementThreshold; }
    public long getSaveInterval() { return saveInterval; }
    public String getApiKey() { return apiKey; }
}
