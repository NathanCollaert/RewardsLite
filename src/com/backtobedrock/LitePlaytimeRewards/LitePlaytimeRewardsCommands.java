package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.helperClasses.ConfigReward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.Reward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RewardsGUI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toMap;
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

    private final LitePlaytimeRewards plugin;

    public LitePlaytimeRewardsCommands() {
        this.plugin = LitePlaytimeRewards.getInstance();
        Bukkit.getServer().getPluginCommand("givereward").setTabCompleter(this);
        Bukkit.getServer().getPluginCommand("rewards").setTabCompleter(this);
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
            case "playtime":
                //check if player
                if (sender == null) {
                    cs.spigot().sendMessage(new ComponentBuilder("You will need to log in to use this command.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                if (this.plugin.getLPRConfig().isCountAllPlaytime()) {
                    cs.spigot().sendMessage(new ComponentBuilder(LitePlaytimeRewardsCommands.timeToString(sender.getStatistic(Statistic.PLAY_ONE_MINUTE),this.plugin.getLPRConfig().))
                            .color(ChatColor.GOLD)
                            .create());
                } else {//get player data
                    LitePlaytimeRewardsCRUD crudplay = this.plugin.getFromOnlineCRUDs(sender.getUniqueId());
                    cs.spigot().sendMessage(new ComponentBuilder(
                            String.format("You have played for %s on this server.",
                                    LitePlaytimeRewardsCommands.timeToString(crudplay.getPlaytime() + crudplay.getAfktime())))
                            .color(ChatColor.GOLD)
                            .create());
                }

                return true;
            case "afktime":
                //check if player
                if (sender == null) {
                    cs.spigot().sendMessage(new ComponentBuilder("You will need to log in to use this command.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                if (this.plugin.ess == null) {
                    cs.spigot().sendMessage(new ComponentBuilder("This server does not keep track of AFK time.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //get player data
                LitePlaytimeRewardsCRUD crudafk = this.plugin.getFromOnlineCRUDs(sender.getUniqueId());

                cs.spigot().sendMessage(new ComponentBuilder(String.format("You have AFK'd for %s on this server.",
                        LitePlaytimeRewardsCommands.timeToString(crudafk.getAfktime())))
                        .color(ChatColor.GOLD)
                        .create());
                return true;
            case "givereward":
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                //check if player
                if (sender == null) {
                    cs.spigot().sendMessage(new ComponentBuilder("You will need to log in to use this command.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //check if rewards available
                TreeMap<String, ConfigReward> giveRewards = this.plugin.getLPRConfig().getRewards();
                if (giveRewards.isEmpty()) {
                    cs.spigot().sendMessage(new ComponentBuilder("No rewards configured in the config file.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //Check if exceeding max inventory size
                if (giveRewards.size() > 54) {
                    cs.spigot().sendMessage(new ComponentBuilder("Max inventory size exceeded, please use /givereward (([reward] [player]) [amount] [broadcast]).")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //Create GUI, add to list and open
                final RewardsGUI giveGUI = new RewardsGUI(giveRewards);
                this.plugin.addToGUIs(sender.getUniqueId(), giveGUI);
                sender.openInventory(giveGUI.getGUI());

                return true;
            case "rewards":
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                //check if player
                if (sender == null) {
                    cs.spigot().sendMessage(new ComponentBuilder("You will need to log in to use this command.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //check if rewards available
                TreeMap<String, Reward> rewards = this.plugin.getFromOnlineCRUDs(sender.getUniqueId()).getRewards();
                if (rewards.isEmpty()) {
                    cs.spigot().sendMessage(new ComponentBuilder("No rewards available for you.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //Check if exceeding max inventory size
                if (rewards.size() > 54) {
                    cs.spigot().sendMessage(new ComponentBuilder("Max inventory size exceeded, please use /rewards <reward>.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //Create GUI and open
                final RewardsGUI rewardsGUI = new RewardsGUI(rewards);
                sender.openInventory(rewardsGUI.getGUI());

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
            case "playtime":
                //check for permission
                if (!cs.hasPermission("liteplaytimerewards.playtime.other")) {
                    cs.spigot().sendMessage(new ComponentBuilder(cmnd.getPermissionMessage())
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                OfflinePlayer plyrplayother = Bukkit.getOfflinePlayer(arg);

                if (this.plugin.getLPRConfig().isCountAllPlaytime()) {
                    if (plyrplayother.isOnline()) {
                        cs.spigot().sendMessage(new ComponentBuilder(String.format("%s has played on this server for %s.",
                                plyrplayother.getName(),
                                LitePlaytimeRewardsCommands.timeToString(((Player) plyrplayother).getStatistic(Statistic.PLAY_ONE_MINUTE))))
                                .color(ChatColor.GOLD)
                                .create());
                        return true;
                    }
                }

                //check if player has played on server before
                if (!LitePlaytimeRewardsCRUD.doesPlayerDataExists(plyrplayother)) {
                    cs.spigot().sendMessage(new ComponentBuilder(String.format("%s has no data on this server yet.", plyrplayother.getName()))
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //get player data
                LitePlaytimeRewardsCRUD crudplayother = new LitePlaytimeRewardsCRUD(plyrplayother);
                cs.spigot().sendMessage(new ComponentBuilder(String.format("%s has played on this server for %s.",
                        plyrplayother.getName(),
                        LitePlaytimeRewardsCommands.timeToString(crudplayother.getPlaytime() + crudplayother.getAfktime())))
                        .color(ChatColor.GOLD)
                        .create());

                return true;
            case "afktime":
                //check for permission
                if (!cs.hasPermission("liteplaytimerewards.afktime.other")) {
                    cs.spigot().sendMessage(new ComponentBuilder(cmnd.getPermissionMessage())
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                if (this.plugin.ess == null) {
                    cs.spigot().sendMessage(new ComponentBuilder("This server does not keep track of AFK time.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //check if player has played on server before
                OfflinePlayer plyrafkother = Bukkit.getOfflinePlayer(arg);

                if (!LitePlaytimeRewardsCRUD.doesPlayerDataExists(plyrafkother)) {
                    cs.spigot().sendMessage(new ComponentBuilder(String.format("%s has no data on this server yet.", plyrafkother.getName()))
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //get player data
                LitePlaytimeRewardsCRUD crudafkother = new LitePlaytimeRewardsCRUD(plyrafkother);

                cs.spigot().sendMessage(new ComponentBuilder(String.format("%s has played for %s on the server.",
                        plyrafkother.getName(),
                        LitePlaytimeRewardsCommands.timeToString(crudafkother.getAfktime())))
                        .color(ChatColor.GOLD)
                        .create());
                return true;
            case "rewards":
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                //check if player
                if (sender == null) {
                    cs.spigot().sendMessage(new ComponentBuilder("You will need to log in to use this command.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //check if rewards available
                TreeMap<String, Reward> rewards = this.plugin.getFromOnlineCRUDs(sender.getUniqueId()).getRewards().entrySet().stream()
                        .filter(e -> e.getKey().equalsIgnoreCase(arg))
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, TreeMap::new));
                if (rewards.isEmpty()) {
                    cs.spigot().sendMessage(new ComponentBuilder("There is no such reward available.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //Create GUI and open
                final RewardsGUI rewardsGUI = new RewardsGUI(rewards);
                sender.openInventory(rewardsGUI.getGUI());

                return true;
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
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                LitePlaytimeRewardsCommands.giveRewardCommand(cs, args[0], args[1], true, 1);
                return true;
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
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                //check if number
                try {
                    Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    cs.spigot().sendMessage(new ComponentBuilder("Amount must be a possitive number greater then 0.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                LitePlaytimeRewardsCommands.giveRewardCommand(cs, args[0], args[1], true, Integer.parseInt(args[2]));
                return true;
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
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                //check if number
                try {
                    Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    cs.spigot().sendMessage(new ComponentBuilder("Amount must be a possitive number greater then 0.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                //check if boolean
                if (!args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false")) {
                    cs.spigot().sendMessage(new ComponentBuilder("Broadcast must be true or false.")
                            .color(ChatColor.RED)
                            .create());
                    return true;
                }

                LitePlaytimeRewardsCommands.giveRewardCommand(cs, args[0], args[1], Boolean.getBoolean(args[3]), Integer.parseInt(args[2]));
                return true;
        }
        return false;
    }

    public static String timeToString(long time, String message) {
        long playtimeInSeconds = time / 20;
        long days = 0, hours = 0, minutes = 0;
        if (playtimeInSeconds >= 86400) {
            days = playtimeInSeconds / 86400;
            playtimeInSeconds -= (days * 86400);
        }
        if (playtimeInSeconds >= 3600) {
            hours = playtimeInSeconds / 3600;
            playtimeInSeconds -= (hours * 3600);
        }
        if (playtimeInSeconds >= 60) {
            minutes = playtimeInSeconds / 60;
            playtimeInSeconds -= (minutes * 60);
        }
        return message.replaceAll("%days%", Long.toString(days))
                .replaceAll("%hours%", Long.toString(hours))
                .replaceAll("%minutes%", Long.toString(minutes))
                .replaceAll("%seconds%", Long.toString(playtimeInSeconds));
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
            case "rewards":
                if (args.length == 1) {
                    StringUtil.copyPartialMatches(args[0].toLowerCase(), this.plugin.getFromOnlineCRUDs(((Player) sender).getUniqueId()).getRewards().keySet(), completions);
                    Collections.sort(completions);
                }
                break;
        }
        return completions;
    }

    public static boolean giveRewardCommand(CommandSender cs, String rewardName, String playerName, boolean broadcast, int amount) {
        LitePlaytimeRewards plugin = LitePlaytimeRewards.getInstance();

        //check if reward exists
        if (!plugin.getLPRConfig().getRewards().containsKey(rewardName.toLowerCase())) {
            cs.spigot().sendMessage(new ComponentBuilder(rewardName + " is not an available reward.")
                    .color(ChatColor.RED)
                    .create());
            return true;
        }

        //check if player is online
        OfflinePlayer giveRewardPlyr = Bukkit.getOfflinePlayer(playerName);
        if (!giveRewardPlyr.isOnline()) {
            cs.spigot().sendMessage(new ComponentBuilder(String.format("The player %s is not online.", giveRewardPlyr.getName()))
                    .color(ChatColor.RED)
                    .create());
            return false;
        }

        //check if amount is bigger then 1
        if (amount < 1) {
            cs.spigot().sendMessage(new ComponentBuilder(amount + " is not a viable amount.")
                    .color(ChatColor.RED)
                    .create());
            return true;
        }

        LitePlaytimeRewardsCRUD crud = plugin.getFromOnlineCRUDs(giveRewardPlyr.getUniqueId());
        TreeMap<String, Reward> rewards = crud.getRewards();

        //if reward is not saved, it should be added with -1 time
        if (!rewards.containsKey(rewardName.toLowerCase())) {
            rewards.put(rewardName.toLowerCase(), new Reward(plugin.getLPRConfig().getRewards().get(rewardName.toLowerCase()), rewardName.toLowerCase(), Arrays.asList(-1L), 0, 0));
        }

        //give reward and set pending
        rewards.get(rewardName.toLowerCase()).setAmountPending(plugin.giveReward(rewards.get(rewardName.toLowerCase()), giveRewardPlyr.getPlayer(), broadcast, amount));

        //save rewards
        crud.setRewards(rewards, true);

        return true;
    }
}
