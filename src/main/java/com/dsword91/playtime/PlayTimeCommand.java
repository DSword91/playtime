package com.dsword91.playtime;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.util.ArrayList;
import java.util.List;

public class PlayTimeCommand extends CommandBase {
    @Override
    public String getName() {
        return "playtime";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/playtime [player] [leaderboard <top>]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            showOwnPlaytime(sender);
        } else if (args[0].equals("leaderboard")) {
            int topN = 10;
            if (args.length > 1) {
                try {
                    topN = parseInt(args[1], 1, 100);
                } catch (Exception e) {
                    topN = 10;
                }
            }
            showLeaderboard(sender, topN);
        } else {
            showPlayerPlaytime(server, sender, args[0]);
        }
    }

    private void showOwnPlaytime(ICommandSender sender) throws CommandException {
        if (!(sender.getCommandSenderEntity() instanceof EntityPlayerMP)) {
            throw new CommandException("只有玩家可以使用此命令");
        }
        EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
        PlayerActiveData data = DataManager.getInstance().getPlayerData(player.getUniqueID().toString());
        String message = String.format("§a%s 的活跃时间: §b%s", player.getName(), formatTime(data.getTotalActiveMinutes()));
        sender.sendMessage(new TextComponentString(message));
    }

    private void showPlayerPlaytime(MinecraftServer server, ICommandSender sender, String playerName) throws CommandException {
        EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playerName);
        if (player == null) {
            throw new CommandException("找不到玩家: " + playerName);
        }
        PlayerActiveData data = DataManager.getInstance().getPlayerData(player.getUniqueID().toString());
        String message = String.format("§a%s 的活跃时间: §b%s", player.getName(), formatTime(data.getTotalActiveMinutes()));
        sender.sendMessage(new TextComponentString(message));
    }

    private void showLeaderboard(ICommandSender sender, int topN) {
        List<DataManager.PlayerRanking> leaderboard = DataManager.getInstance().getLeaderboard(topN);
        if (leaderboard.isEmpty()) {
            sender.sendMessage(new TextComponentString("§e📊 暂无玩家数据"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("§e📊 活跃时间排行榜 (共").append(leaderboard.size()).append("人)\n§7━━━━━━━━━━━━━━\n");
        String[] medals = {"§6🥇", "§7🥈", "§3🥉"};

        for (int i = 0; i < leaderboard.size(); i++) {
            DataManager.PlayerRanking r = leaderboard.get(i);
            String rank = i < 3 ? medals[i] : "§f" + (i + 1) + ".";
            sb.append(rank).append(" ").append(r.playerName != null ? r.playerName : "未知")
              .append(" — §b").append(formatTime(r.activeMinutes)).append("\n");
        }
        sender.sendMessage(new TextComponentString(sb.toString()));
    }

    private String formatTime(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return hours > 0 ? hours + "小时" + mins + "分" : mins + "分钟";
    }

    @Override
    public List<String> getTabCompletions(ICommandSender sender, String[] args) {
        net.minecraft.server.MinecraftServer server = net.minecraft.server.MinecraftServer.getServer();
        if (server == null) return new ArrayList<>();
        
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("leaderboard");
            for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
                completions.add(player.getName());
            }
            return getListOfStringsMatchingLastWord(args, completions.toArray(new String[0]));
        } else if (args.length == 2 && args[0].equals("leaderboard")) {
            return getListOfStringsMatchingLastWord(args, "10", "20", "50", "100");
        }
        return new ArrayList<>();
    }
}
