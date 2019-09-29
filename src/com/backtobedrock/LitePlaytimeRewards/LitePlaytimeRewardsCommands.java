package com.backtobedrock.LitePlaytimeRewards;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LitePlaytimeRewardsCommands {

    private LitePlaytimeRewards plugin = null;

    public LitePlaytimeRewardsCommands(LitePlaytimeRewards plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        switch (args.length) {
            case 0:
                return this.zeroParameters(cs, cmnd);
            case 1:
                return this.oneParameter(cs, cmnd, args[0]);
            case 2:
                return this.twoParameters(cs, cmnd, args);
            default:
                return false;
        }
    }

    private boolean zeroParameters(CommandSender cs, Command cmnd) {
        Player sender = null;
        if (cs instanceof Player) {
            sender = (Player) cs;
        }
        switch (cmnd.getName().toLowerCase()) {
            case "checkplaytime":
                //check if player
                if (sender == null) {
                    cs.spigot().sendMessage(new ComponentBuilder("You'll need to log in to use this command.").color(ChatColor.RED).create());
                    return true;
                }
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                cs.spigot().sendMessage(new ComponentBuilder(String.format("You have played for %s on this server.", this.playtimeToString(sender))).color(ChatColor.GOLD).create());
                return true;
            case "redeemrewards":
                //check if player
                if (sender == null) {
                    cs.spigot().sendMessage(new ComponentBuilder("You'll need to log in to use this command.").color(ChatColor.RED).create());
                    return true;
                }
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                if (!this.plugin.checkEligibleForRewards(sender)) {
                    cs.spigot().sendMessage(new ComponentBuilder("You aren't eligible for any rewards.").color(ChatColor.YELLOW).create());
                }
                return true;
        }
        return false;
    }

    private boolean oneParameter(CommandSender cs, Command cmnd, String arg) {
        Player sender = null;
        if (cs instanceof Player) {
            sender = (Player) cs;
        }
        switch (cmnd.getName().toLowerCase()) {
            case "checkplaytime":
                //check for permission
                if (!cs.hasPermission("liteplaytimerewards.checkplaytime.other")) {
                    return true;
                }

                OfflinePlayer checkPlaytimePlyr = Bukkit.getOfflinePlayer(arg);
                if (!checkPlaytimePlyr.isOnline()) {
                    cs.spigot().sendMessage(new ComponentBuilder(String.format("%s isn't online.", checkPlaytimePlyr.getName())).color(ChatColor.RED).create());
                    return true;
                }
                cs.spigot().sendMessage(new ComponentBuilder(String.format("%s has played for %s on the server.", checkPlaytimePlyr.getPlayer().getName(), this.playtimeToString(checkPlaytimePlyr.getPlayer()))).color(ChatColor.GOLD).create());
                return true;
            case "setpt":
                sender.setStatistic(Statistic.PLAY_ONE_MINUTE, sender.getStatistic(Statistic.PLAY_ONE_MINUTE) + 60 * 60 * 20);
                return true;
        }
        return false;
    }

    private boolean twoParameters(CommandSender cs, Command cmnd, String[] args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String playtimeToString(Player plyr) {
        if (plyr.getStatistic(Statistic.PLAY_ONE_MINUTE) >= 20) {
            long playtimeInSeconds = plyr.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;
            StringBuilder sb = new StringBuilder();
            if (playtimeInSeconds >= 86400) {
                long days = playtimeInSeconds / 86400;
                sb.append(days).append(" days");
                playtimeInSeconds -= (days * 86400);
                if (playtimeInSeconds != 0) {
                    sb.append(", ");
                }
            }
            if (playtimeInSeconds >= 3600) {
                long hours = playtimeInSeconds / 3600;
                sb.append(hours).append(" hours");
                playtimeInSeconds -= (hours * 3600);
                if (playtimeInSeconds != 0) {
                    sb.append(", ");
                }
            }
            if (playtimeInSeconds >= 60) {
                long minutes = playtimeInSeconds / 60;
                sb.append(minutes).append(" minutes");
                playtimeInSeconds -= (minutes * 60);
                if (playtimeInSeconds != 0) {
                    sb.append(" and ");
                }
            }
            if (playtimeInSeconds >= 1) {
                sb.append(playtimeInSeconds).append(" seconds");
            }
            return sb.toString();
        }
        return "less then a second";
    }
}
