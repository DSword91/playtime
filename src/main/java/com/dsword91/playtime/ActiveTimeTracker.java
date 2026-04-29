package com.dsword91.playtime;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActiveTimeTracker {
    private static final Logger LOGGER = LogManager.getLogger(ActiveTimeTracker.class);
    private static final Map<UUID, Long> playerLastActivity = new HashMap<>();
    private static final Map<UUID, double[]> playerLastPosition = new HashMap<>();
    private static final long AFK_THRESHOLD = 100;
    private static final double MOVEMENT_THRESHOLD = 0.1;
    private static long saveCounter = 0;
    private static final long SAVE_INTERVAL = 6000;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        var server = event.getServer();
        long currentTick = server.getTickCounter();

        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUniqueID();
            String playerName = player.getName();
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
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof EntityPlayerMP)) return;
        
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        UUID uuid = player.getUniqueID();
        double[] lastPos = playerLastPosition.get(uuid);

        if (lastPos != null) {
            double dx = player.posX - lastPos[0];
            double dy = player.posY - lastPos[1];
            double dz = player.posZ - lastPos[2];
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (distance > MOVEMENT_THRESHOLD) {
                markPlayerActive(player);
                lastPos[0] = player.posX;
                lastPos[1] = player.posY;
                lastPos[2] = player.posZ;
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.player.getUniqueID();
        DataManager.getInstance().saveData();
        playerLastActivity.remove(uuid);
        playerLastPosition.remove(uuid);
        LOGGER.info("玩家 {} 登出，已保存活跃时间数据", event.player.getName());
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.player;
            UUID uuid = player.getUniqueID();
            playerLastPosition.put(uuid, new double[]{player.posX, player.posY, player.posZ});
            LOGGER.info("玩家 {} 登录，开始追踪活跃时间", player.getName());
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.player instanceof EntityPlayerMP) markPlayerActive((EntityPlayerMP) event.player);
    }

    @SubscribeEvent
    public void onPlayerInteract(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntityPlayer() instanceof EntityPlayerMP) markPlayerActive((EntityPlayerMP) event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onPlayerUseItem(net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityPlayer() instanceof EntityPlayerMP) markPlayerActive((EntityPlayerMP) event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onPlayerAttack(net.minecraftforge.event.entity.player.AttackEntityEvent event) {
        if (event.getEntityPlayer() instanceof EntityPlayerMP) markPlayerActive((EntityPlayerMP) event.getEntityPlayer());
    }

    @SubscribeEvent
    public void onChatMessage(net.minecraftforge.event.ServerChatEvent event) {
        markPlayerActive(event.getPlayer());
    }

    private static void markPlayerActive(EntityPlayerMP player) {
        UUID uuid = player.getUniqueID();
        long currentTick = player.world.getMinecraftServer().getTickCounter();
        playerLastActivity.put(uuid, currentTick);
        PlayerActiveData data = DataManager.getInstance().getPlayerData(uuid.toString());
        data.recordActivity(player.getName(), currentTick);
    }

    @SubscribeEvent
    public void onServerStopped(net.minecraftforge.fml.common.event.FMLServerStoppedEvent event) {
        DataManager.getInstance().saveData();
        LOGGER.info("服务器停止，已保存所有玩家活跃时间数据");
    }
}
