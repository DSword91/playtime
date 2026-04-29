package com.dsword91.playtime;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.Logger;

@Mod(modid = PlayTimeMod.MOD_ID, name = PlayTimeMod.NAME, version = PlayTimeMod.VERSION)
public class PlayTimeMod {
    public static final String MOD_ID = "playtime";
    public static final String NAME = "PlayTime";
    public static final String VERSION = "1.0.0";
    
    private static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("PlayTime 模组预初始化 - 准备追踪玩家活跃时间");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("PlayTime 模组已初始化 - 开始追踪玩家活跃时间");
        
        // 注册事件监听器
        MinecraftForge.EVENT_BUS.register(new ActiveTimeTracker());
        
        DataManager.getInstance().loadData();
        logger.info("玩家活跃数据已加载");
    }

    @Mod.EventHandler
    public void serverStarting(net.minecraftforge.fml.common.event.FMLServerStartingEvent event) {
        PluginConfig config = PluginConfig.getInstance();
        HttpApiServer.startServer(config.getApiPort());
        logger.info("HTTP API 服务器已启动在端口 {}", config.getApiPort());
        
        event.registerServerCommand(new PlayTimeCommand());
        logger.info("PlayTime 命令已注册");
    }
}
