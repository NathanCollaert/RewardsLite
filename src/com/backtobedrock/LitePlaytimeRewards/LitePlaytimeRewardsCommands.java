package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.helperClasses.ConfigReward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.Reward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RewardsGUI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toMap;
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
        Player sender = cs instanceof Player ? (Player) cs : null;
        switch (cmnd.getName().toLowerCase()) {
            case "playtime":
                //check if player
                if (sender == null) {
                    cs.sendMessage(this.plugin.getMessages().getNeedToBeOnline());
                    return true;
                }

                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                if (this.plugin.getLPRConfig().isCountAllPlaytime()) {
                    cs.sendMessage(this.plugin.getMessages().getPlaytime(sender.getStatistic(Statistic.PLAY_ONE_MINUTE)));
                } else {//get player data
                    LitePlaytimeRewardsCRUD crudplay = this.plugin.getFromCRUDCache(sender.getUniqueId());
                    cs.sendMessage(this.plugin.getMessages().getPlaytime(crudplay.getPlaytime() + crudplay.getAfktime()));
                }

                return true;
            case "afktime":
                //check if player
                if (sender == null) {
                    cs.sendMessage(this.plugin.getMessages().getNeedToBeOnline());
                    return true;
                }

                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                if (this.plugin.ess == null) {
                    cs.sendMessage(this.plugin.getMessages().getServerDoesntKeepTrackOfAFK());
                    return true;
                }

                //get player data
                LitePlaytimeRewardsCRUD crudafk = this.plugin.getFromCRUDCache(sender.getUniqueId());

                cs.sendMessage(this.plugin.getMessages().getAFKTime(crudafk.getAfktime()));
                return true;
            case "givereward":
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                //check if player
                if (sender == null) {
                    cs.sendMessage(this.plugin.getMessages().getNeedToBeOnline());
                    return true;
                }

                //check if rewards available
                TreeMap<String, ConfigReward> giveRewards = this.plugin.getLPRConfig().getRewards();
                if (giveRewards.isEmpty()) {
                    cs.sendMessage(this.plugin.getMessages().getNoRewardsConfigured());
                    return true;
                }

                //Check if exceeding max inventory size
                if (giveRewards.size() > 54) {
                    cs.sendMessage(this.plugin.getMessages().getMaxInventExceeded("/givereward <reward> <player> [amount] [broadcast]"));
                    return true;
                }

                //Create GUI, add to list and open
                final RewardsGUI giveGUI = new RewardsGUI(giveRewards, sender);
                this.plugin.addToGUICache(sender.getUniqueId(), giveGUI);
                sender.openInventory(giveGUI.getGUI());

                return true;
            case "rewards":
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                //check if player
                if (sender == null) {
                    cs.sendMessage(this.plugin.getMessages().getNeedToBeOnline());
                    return true;
                }

                //check if rewards available
                TreeMap<String, Reward> rewards = this.plugin.getFromCRUDCache(sender.getUniqueId()).getRewards();
                if (rewards.isEmpty()) {
                    cs.sendMessage(this.plugin.getMessages().getNoRewardsAvailable());
                    return true;
                }

                //Check if exceeding max inventory size
                if (rewards.size() > 54) {
                    cs.sendMessage(this.plugin.getMessages().getMaxInventExceeded("/rewards <reward>"));
                    return true;
                }

                //Create GUI and open
                TreeMap<String, Reward> filteredRewards = rewards.entrySet().stream()
                        .filter(e -> (!e.getValue().getcReward().isUsePermission() || sender.hasPermission("liteplaytimerewards.reward." + e.getKey()))
                        || e.getValue().getAmountPending() > 0
                        || e.getValue().getAmountRedeemed() > 0)
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, TreeMap::new));
                RewardsGUI rewardsGUI = new RewardsGUI(filteredRewards, sender);
                sender.openInventory(rewardsGUI.getGUI());

                return true;
        }
        return false;
    }

    private boolean oneParameter(CommandSender cs, Command cmnd, String arg) {
        Player sender = cs instanceof Player ? (Player) cs : null;
        switch (cmnd.getName().toLowerCase()) {
            case "playtime":
                //check for permission
                if (!cs.hasPermission("liteplaytimerewards.playtime.other")) {
                    cs.sendMessage(this.plugin.getMessages().getNoPermission());
                    return true;
                }

                OfflinePlayer plyrplayother = Bukkit.getOfflinePlayer(arg);

                if (this.plugin.getLPRConfig().isCountAllPlaytime() && plyrplayother.isOnline()) {
                    cs.sendMessage(this.plugin.getMessages().getPlaytimeOther(((Player) plyrplayother).getStatistic(Statistic.PLAY_ONE_MINUTE), plyrplayother.getName()));
                    return true;
                }

                //check if player has played on server before
                if (!LitePlaytimeRewardsCRUD.doesPlayerDataExists(plyrplayother)) {
                    cs.sendMessage(this.plugin.getMessages().getNoData(plyrplayother.getName()));
                    return true;
                }

                //get player data
                LitePlaytimeRewardsCRUD crudplayother = plyrplayother.isOnline()
                        ? this.plugin.getFromCRUDCache(plyrplayother.getUniqueId())
                        : new LitePlaytimeRewardsCRUD(plyrplayother);
                cs.sendMessage(this.plugin.getMessages().getPlaytimeOther(crudplayother.getPlaytime() + crudplayother.getAfktime(), plyrplayother.getName()));

                return true;
            case "afktime":
                //check for permission
                if (!cs.hasPermission("liteplaytimerewards.afktime.other")) {
                    cs.sendMessage(this.plugin.getMessages().getNoPermission());
                    return true;
                }

                if (this.plugin.ess == null) {
                    cs.sendMessage(this.plugin.getMessages().getServerDoesntKeepTrackOfAFK());
                    return true;
                }

                //check if player has played on server before
                OfflinePlayer plyrafkother = Bukkit.getOfflinePlayer(arg);

                if (!LitePlaytimeRewardsCRUD.doesPlayerDataExists(plyrafkother)) {
                    cs.sendMessage(this.plugin.getMessages().getNoData(plyrafkother.getName()));
                    return true;
                }

                //get player data
                LitePlaytimeRewardsCRUD crudafkother = plyrafkother.isOnline()
                        ? this.plugin.getFromCRUDCache(plyrafkother.getUniqueId())
                        : new LitePlaytimeRewardsCRUD(plyrafkother);

                cs.sendMessage(this.plugin.getMessages().getAFKTimeOther(crudafkother.getAfktime(), plyrafkother.getName()));
                return true;
            case "rewards":
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                //check if player
                if (sender == null) {
                    cs.sendMessage(this.plugin.getMessages().getNeedToBeOnline());
                    return true;
                }

                //check if reward available
                TreeMap<String, Reward> rewards = this.plugin.getFromCRUDCache(sender.getUniqueId()).getRewards().entrySet().stream()
                        .filter(e -> e.getKey().equalsIgnoreCase(arg))
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, TreeMap::new));
                if (rewards.isEmpty()) {
                    cs.sendMessage(this.plugin.getMessages().getNoSuchReward(arg));
                    return true;
                }

                //Create GUI and open
                final RewardsGUI rewardsGUI = new RewardsGUI(rewards, sender);
                sender.openInventory(rewardsGUI.getGUI());

                return true;
        }
        return false;
    }

    private boolean twoParameters(CommandSender cs, Command cmnd, String[] args) {
        Player sender = cs instanceof Player ? (Player) cs : null;
        switch (cmnd.getName().toLowerCase()) {
            case "givereward":
                //check for permission
                if (!cmnd.testPermission(cs)) {
                    return true;
                }

                if (LitePlaytimeRewardsCommands.giveRewardCommand(cs, args[0], args[1], true, 1)) {
                    cs.sendMessage(this.plugin.getMessages().getRewardGiven(args[1], args[0]));
                }
                return true;
        }
        return false;
    }

    private boolean threeParameters(CommandSender cs, Command cmnd, String[] args) {
        Player sender = cs instanceof Player ? (Player) cs : null;
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
                    cs.sendMessage(this.plugin.getMessages().getNotANumber());
                    return true;
                }

                if (LitePlaytimeRewardsCommands.giveRewardCommand(cs, args[0], args[1], true, Integer.parseInt(args[2]))) {
                    cs.sendMessage(this.plugin.getMessages().getRewardGiven(args[1], args[0]));
                }

                return true;
        }
        return false;
    }

    private boolean fourParameters(CommandSender cs, Command cmnd, String[] args) {
        Player sender = cs instanceof Player ? (Player) cs : null;
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
                    cs.sendMessage(this.plugin.getMessages().getNotANumber());
                    return true;
                }

                //check if boolean
                if (!args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false")) {
                    cs.sendMessage(this.plugin.getMessages().getNotABoolean());
                    return true;
                }

                if (LitePlaytimeRewardsCommands.giveRewardCommand(cs, args[0], args[1], Boolean.getBoolean(args[3]), Integer.parseInt(args[2]))) {
                    cs.sendMessage(this.plugin.getMessages().getRewardGiven(args[1], args[0]));
                }

                return true;
        }
        return false;
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
                    StringUtil.copyPartialMatches(args[0].toLowerCase(), this.plugin.getFromCRUDCache(((Player) sender).getUniqueId()).getRewards().keySet(), completions);
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
            cs.sendMessage(LitePlaytimeRewards.getInstance().getMessages().getNoSuchReward(rewardName));
            return false;
        }

        //check if amount is bigger then 1
        if (amount < 1) {
            cs.sendMessage(LitePlaytimeRewards.getInstance().getMessages().getNotANumber());
            return false;
        }

        //check if player has data on server
        OfflinePlayer plyr = Bukkit.getOfflinePlayer(playerName);
        if (!LitePlaytimeRewardsCRUD.doesPlayerDataExists(plyr)) {
            cs.sendMessage(LitePlaytimeRewards.getInstance().getMessages().getNoData(plyr.getName()));
            return false;
        }

        LitePlaytimeRewardsCRUD crud = plyr.isOnline() ? plugin.getFromCRUDCache(plyr.getUniqueId()) : new LitePlaytimeRewardsCRUD(plyr);

        //get reward
        Reward reward = crud.getRewards().get(rewardName.toLowerCase());

        if (reward != null) {
            //give reward and set pending
            reward.setAmountPending(plugin.giveReward(reward, plyr, broadcast, amount));
            crud.saveConfig();
        }

        return true;
    }
}
