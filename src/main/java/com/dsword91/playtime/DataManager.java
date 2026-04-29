package com.dsword91.playtime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {
    private static final Logger LOGGER = LogManager.getLogger(DataManager.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DATA_DIR = "config";
    private static final String DATA_FILE = DATA_DIR + "/playtime_data.json";

    private static DataManager instance;
    private final Map<String, PlayerActiveData> playerData = new ConcurrentHashMap<>();

    private DataManager() {}

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public PlayerActiveData getPlayerData(String uuid) {
        return playerData.computeIfAbsent(uuid, k -> new PlayerActiveData());
    }

    public void saveData() {
        try {
            Path dataDir = Paths.get(DATA_DIR);
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            Path dataPath = Paths.get(DATA_FILE);
            String json = GSON.toJson(playerData);
            Files.write(dataPath, json.getBytes("UTF-8"));
            LOGGER.info("已保存 {} 个玩家的活跃时间数据", playerData.size());
        } catch (IOException e) {
            LOGGER.error("保存玩家数据失败", e);
        }
    }

    public void loadData() {
        try {
            Path dataPath = Paths.get(DATA_FILE);
            if (!Files.exists(dataPath)) {
                LOGGER.info("未找到数据文件，将创建新的数据文件");
                return;
            }
            String json = new String(Files.readAllBytes(dataPath), "UTF-8");
            Type type = new TypeToken<Map<String, PlayerActiveData>>() {}.getType();
            Map<String, PlayerActiveData> loaded = GSON.fromJson(json, type);
            if (loaded != null) {
                playerData.clear();
                playerData.putAll(loaded);
                LOGGER.info("已加载 {} 个玩家的活跃时间数据", playerData.size());
            }
        } catch (IOException e) {
            LOGGER.error("加载玩家数据失败", e);
        }
    }

    public List<PlayerRanking> getLeaderboard(int topN) {
        List<PlayerRanking> rankings = new ArrayList<>();
        for (Map.Entry<String, PlayerActiveData> entry : playerData.entrySet()) {
            if (entry.getValue().getTotalActiveMinutes() > 0) {
                rankings.add(new PlayerRanking(
                        entry.getKey(),
                        entry.getValue().getPlayerName(),
                        entry.getValue().getTotalActiveMinutes()
                ));
            }
        }
        rankings.sort((a, b) -> Integer.compare(b.activeMinutes, a.activeMinutes));
        return rankings.subList(0, Math.min(topN, rankings.size()));
    }

    public void periodicSave() {
        saveData();
    }

    public static class PlayerRanking {
        public final String uuid;
        public final String playerName;
        public final int activeMinutes;

        public PlayerRanking(String uuid, String playerName, int activeMinutes) {
            this.uuid = uuid;
            this.playerName = playerName;
            this.activeMinutes = activeMinutes;
        }
    }
}
