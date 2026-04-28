package com.dsword91.playtime;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import org.slf4j.Logger;

import java.util.*;

@EventBusSubscriber(modid = PlayTimeMod.MOD_ID)
public class ActiveTimeTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<UUID, Long> playerLastActivity = new HashMap<>();
    private static final Map<UUID, double[]> playerLastPosition = new HashMap<>();
    private static final long AFK_THRESHOLD = 100;
    private static final double MOVEMENT_THRESHOLD = 0.1;
    private static long saveCounter = 0;
    private static final long SAVE_INTERVAL = 6000;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        long currentTick = server.getTickCount();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            String playerName = player.getName().getString();
            PlayerActiveData data = DataManager.getInstance().getPlayerData(uuid.toString());
            data.setPlayerName(playerName);

            Long lastActivity = playerLastActivity.get(uuid);
            if (lastActivity != null && (currentTick - lastActivity) <= AFK_THRESHOLD) {
                data.recordActivity(playerName, currentTick);
            } else {
                data.setWasActive(false);
            }
            data.updateActiveTime(currentTick);
        }

        saveCounter++;
        if (saveCounter >= SAVE_INTERVAL) {
            DataManager.getInstance().periodicSave();
            saveCounter = 0;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID uuid = player.getUUID();
        double[] lastPos = playerLastPosition.get(uuid);

        if (lastPos != null) {
            double dx = player.getX() - lastPos[0];
            double dy = player.getY() - lastPos[1];
            double dz = player.getZ() - lastPos[2];
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > MOVEMENT_THRESHOLD) {
                markPlayerActive(player);
                lastPos[0] = player.getX();
                lastPos[1] = player.getY();
                lastPos[2] = player.getZ();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        DataManager.getInstance().saveData();
        playerLastActivity.remove(uuid);
        playerLastPosition.remove(uuid);
        LOGGER.info("玩家 {} 登出，已保存活跃时间数据", event.getEntity().getName().getString());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UUID uuid = player.getUUID();
            playerLastPosition.put(uuid, new double[]{player.getX(), player.getY(), player.getZ()});
            LOGGER.info("玩家 {} 登录，开始追踪活跃时间", player.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) markPlayerActive(player);
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player) markPlayerActive(player);
    }

    @SubscribeEvent
    public static void onPlayerUseItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() instanceof ServerPlayer player) markPlayerActive(player);
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) markPlayerActive(player);
    }

    @SubscribeEvent
    public static void onChatMessage(net.neoforged.neoforge.event.ServerChatEvent event) {
        markPlayerActive(event.getPlayer());
    }

    private static void markPlayerActive(ServerPlayer player) {
        UUID uuid = player.getUUID();
        long currentTick = player.serverLevel().getServer().getTickCount();
        playerLastActivity.put(uuid, currentTick);
        PlayerActiveData data = DataManager.getInstance().getPlayerData(uuid.toString());
        data.recordActivity(player.getName().getString(), currentTick);
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        DataManager.getInstance().saveData();
        LOGGER.info("服务器停止，已保存所有玩家活跃时间数据");
    }
}
