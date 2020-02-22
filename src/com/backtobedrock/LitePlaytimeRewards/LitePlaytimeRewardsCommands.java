package com.backtobedrock.LitePlaytimeRewards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

public class LitePlaytimeRewardsCommands implements TabCompleter {

    private LitePlaytimeRewards plugin = null;

    public LitePlaytimeRewardsCommands(LitePlaytimeRewards plugin) {
        this.plugin = plugin;
        Bukkit.getServer().getPluginCommand("givereward").setTabCompleter(this);
    }

    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        switch (args.length) {
            case 0:
                return this.zeroParameters(cs, cmnd);
            case 1:
                return this.oneParameter(cs, cmnd, args[0]);
            case 2:
                return this.twoParameters(cs, cmnd, args);
            case 3:
                return this.threeParameters(cs, cmnd, args);
            case 4:
                return this.fourParameters(cs, cmnd, args);
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
                    cs.spigot().sendMessage(new ComponentBuilder("You will need to log in to use this command.").color(ChatColor.RED).create());
                    return true;
                }
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                cs.spigot().sendMessage(new ComponentBuilder(String.format("You have played for %s on this server.", this.playtimeToString(sender))).color(ChatColor.GOLD).create());
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
//            case "checkplaytime":
//                //check for permission
//                if (!cs.hasPermission("liteplaytimerewards.checkplaytime.other")) {
//                    return true;
//                }
//
//                OfflinePlayer checkPlaytimePlyr = Bukkit.getOfflinePlayer(arg);
//                if (!checkPlaytimePlyr.isOnline()) {
//                    cs.spigot().sendMessage(new ComponentBuilder(String.format("%s is not online.", checkPlaytimePlyr.getName())).color(ChatColor.RED).create());
//                    return true;
//                }
//                cs.spigot().sendMessage(new ComponentBuilder(String.format("%s has played for %s on the server.", checkPlaytimePlyr.getPlayer().getName(), this.playtimeToString(checkPlaytimePlyr.getPlayer()))).color(ChatColor.GOLD).create());
//                return true;
        }
        return false;
    }

    private boolean twoParameters(CommandSender cs, Command cmnd, String[] args) {
        Player sender = null;
        if (cs instanceof Player) {
            sender = (Player) cs;
        }
        switch (cmnd.getName().toLowerCase()) {
            case "givereward":
                return this.giveRewardCommand(cs, cmnd, args, true, 1);
        }
        return false;
    }

    private boolean threeParameters(CommandSender cs, Command cmnd, String[] args) {
        Player sender = null;
        if (cs instanceof Player) {
            sender = (Player) cs;
        }
        switch (cmnd.getName().toLowerCase()) {
            case "givereward":
                try {
                    Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    cs.spigot().sendMessage(new ComponentBuilder("Amount must be a possitive number greater then 0.").color(ChatColor.RED).create());
                    return true;
                }

                return this.giveRewardCommand(cs, cmnd, args, true, Integer.parseInt(args[2]));
        }
        return false;
    }

    private boolean fourParameters(CommandSender cs, Command cmnd, String[] args) {
        Player sender = null;
        if (cs instanceof Player) {
            sender = (Player) cs;
        }
        switch (cmnd.getName().toLowerCase()) {
            case "givereward":
                try {
                    Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    cs.spigot().sendMessage(new ComponentBuilder("Amount must be a possitive number greater then 0.").color(ChatColor.RED).create());
                    return true;
                }

                if (!args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false")) {
                    cs.spigot().sendMessage(new ComponentBuilder("Broadcast must be true or false.").color(ChatColor.RED).create());
                    return true;
                }

                return this.giveRewardCommand(cs, cmnd, args, Boolean.getBoolean(args[3]), Integer.parseInt(args[2]));
        }
        return false;
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmnd, String alias, String[] args) {
        //create new array
        final List<String> completions = new ArrayList<>();

        switch (cmnd.getName().toLowerCase()) {
            case "givereward":
                if (args.length == 1) {
                    StringUtil.copyPartialMatches(args[0].toLowerCase(), this.plugin.getLPRConfig().getRewards().keySet(), completions);
                    Collections.sort(completions);
                }
                if (args.length == 2) {
                    completions.addAll(this.plugin.getServer().getOnlinePlayers().stream().map(e -> e.getName()).collect(Collectors.toList()));
                }
                break;
        }
        return completions;
    }

    private boolean giveRewardCommand(CommandSender cs, Command cmnd, String[] args, boolean broadcast, int amount) {
        //check for permission
        if (!cmnd.testPermission(cs)) {
            return true;
        }

        if (!this.plugin.getLPRConfig().getRewards().containsKey(args[0].toLowerCase())) {
            cs.spigot().sendMessage(new ComponentBuilder(args[0] + " is not an available reward.").color(ChatColor.RED).create());
            return true;
        }

        OfflinePlayer giveRewardPlyr = Bukkit.getOfflinePlayer(args[1]);
        if (!giveRewardPlyr.isOnline()) {
            cs.spigot().sendMessage(new ComponentBuilder(String.format("%s is not online.", giveRewardPlyr.getName())).color(ChatColor.RED).create());
            return true;
        }

        if (amount < 1) {
            cs.spigot().sendMessage(new ComponentBuilder(amount + " is not a viable amount.").color(ChatColor.RED).create());
            return true;
        }

        this.plugin.giveRewardAndNotify(this.plugin.getLPRConfig().getRewards().get(args[0].toLowerCase()), giveRewardPlyr.getPlayer(), broadcast, amount);

        return true;
    }
}
