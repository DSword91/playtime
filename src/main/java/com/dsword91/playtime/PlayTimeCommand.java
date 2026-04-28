package com.dsword91.playtime;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.List;

@EventBusSubscriber(modid = PlayTimeMod.MOD_ID)
public class PlayTimeCommand {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        var dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("playtime")
                .executes(PlayTimeCommand::showOwnPlaytime)
                .then(Commands.argument("player", net.minecraft.commands.arguments.GameProfileArgument.gameProfile())
                        .executes(PlayTimeCommand::showPlayerPlaytime))
                .then(Commands.literal("leaderboard")
                        .executes(context -> showLeaderboard(context, 10))
                        .then(Commands.argument("top", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 100))
                                .executes(context -> showLeaderboard(context, com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "top")))))
        );
    }

    private static int showOwnPlaytime(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        PlayerActiveData data = DataManager.getInstance().getPlayerData(player.getUUID().toString());
        String message = String.format("§a%s 的活跃时间: §b%s", player.getName().getString(), formatTime(data.getTotalActiveMinutes()));
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int showPlayerPlaytime(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var profiles = net.minecraft.commands.arguments.GameProfileArgument.getGameProfiles(context, "player");
        for (var profile : profiles) {
            PlayerActiveData data = DataManager.getInstance().getPlayerData(profile.getId().toString());
            String name = profile.getName() != null ? profile.getName() : "未知玩家";
            String message = String.format("§a%s 的活跃时间: §b%s", name, formatTime(data.getTotalActiveMinutes()));
            context.getSource().sendSuccess(() -> Component.literal(message), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int showLeaderboard(CommandContext<CommandSourceStack> context, int topN) {
        List<DataManager.PlayerRanking> leaderboard = DataManager.getInstance().getLeaderboard(topN);
        if (leaderboard.isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("§e📊 暂无玩家数据"), false);
            return Command.SINGLE_SUCCESS;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§e📊 活跃时间排行榜 (共").append(leaderboard.size()).append("人)\n§7━━━━━━━━━━━━━━\n");
        String[] medals = {"§6🥇", "§7🥈", "§3🥉"};

        for (int i = 0; i < leaderboard.size(); i++) {
            var r = leaderboard.get(i);
            String rank = i < 3 ? medals[i] : "§f" + (i + 1) + ".";
            sb.append(rank).append(" ").append(r.playerName() != null ? r.playerName() : "未知")
              .append(" — §b").append(formatTime(r.activeMinutes())).append("\n");
        }
        context.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static String formatTime(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return hours > 0 ? hours + "小时" + mins + "分" : mins + "分钟";
    }
}
