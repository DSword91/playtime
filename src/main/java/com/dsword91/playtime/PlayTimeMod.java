package com.dsword91.playtime;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(PlayTimeMod.MOD_ID)
public class PlayTimeMod {
    public static final String MOD_ID = "playtime";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PlayTimeMod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("PlayTime 模组已初始化 - 开始追踪玩家活跃时间");
        modEventBus.addListener(PlayTimeMod::commonSetup);
    }

    private static void commonSetup(net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent event) {
        DataManager.getInstance().loadData();
        LOGGER.info("玩家活跃数据已加载");
    }
}
