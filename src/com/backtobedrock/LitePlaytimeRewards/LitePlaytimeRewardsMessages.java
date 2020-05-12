package com.backtobedrock.LitePlaytimeRewards;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class LitePlaytimeRewardsMessages {

    private final LitePlaytimeRewards plugin;
    private TreeMap<String, Object> messages = new TreeMap<>();

    public LitePlaytimeRewardsMessages(File messagesFile) {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        YamlConfiguration.loadConfiguration(messagesFile).getValues(true).entrySet().forEach((e) -> {
            this.messages.put(e.getKey(), e.getValue());
        });
    }

    //<editor-fold desc="RewardsGUI" defaultstate="collapsed">
    public String getRewardsInventoryTitle() {
        return this.messages.getOrDefault("RewardsInventoryTitle", "&3Your Rewards").toString().replaceAll("&", "§");
    }

    public String getGiveRewardInventoryTitle() {
        return this.messages.getOrDefault("GiveRewardInventoryTitle", "&2Available Rewards").toString().replaceAll("&", "§");
    }

    public List<String> getRewardInfo(int redeemed, int pending) {
        List<String> rewardInfo = ((List<String>) this.messages.getOrDefault("RewardInfo", Arrays.asList("&a(redeemed: &2%redeemed%&a, pending: &2%pending%&a)")));
        return rewardInfo.stream()
                .map(e -> e.replaceAll("%redeemed%", Integer.toString(redeemed)).replaceAll("%pending%", Integer.toString(pending)).replaceAll("&", "§"))
                .collect(Collectors.toList());
    }

    public List<String> getNextReward(int time) {
        List<String> nextReward = (List<String>) this.messages.getOrDefault("NextReward", Arrays.asList("&eNext reward in &6", "&6%days% days, %hours% hours, %minutes% mins, %seconds% secs"));
        return nextReward.stream()
                .map(e -> this.timeToString(time, e.replaceAll("&", "§")))
                .collect(Collectors.toList());
    }

    public List<String> getNextRewardNever() {
        List<String> nextRewardNever = (List<String>) this.messages.getOrDefault("NextRewardNever", Arrays.asList("&cClaimed all rewards already."));
        return nextRewardNever.stream()
                .map(e -> e.replaceAll("&", "§"))
                .collect(Collectors.toList());
    }

    public List<String> getNextRewardNoPermission() {
        List<String> nextRewardNoPerm = (List<String>) this.messages.getOrDefault("NextRewardNoPermission", Arrays.asList("&cYou can't earn this reward."));
        return nextRewardNoPerm.stream()
                .map(e -> e.replaceAll("&", "§"))
                .collect(Collectors.toList());
    }

    public String getPendingNotificationWrongWorld(String player, String rewardname, String world) {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("PendingNotificationWrongWorld", "&eYou have a playtime reward pending but can't claim it in this world.").toString()
                .replaceAll("%player%", player)
                .replaceAll("%rewardname%", rewardname)
                .replaceAll("%world%", world));
    }

    public String getPendingNotificationNotEnoughInventory(String player, String rewardname, int inventoryNeeded) {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("PendingNotificationNotEnoughInventory", "&eYou need &6%inventory_spaces_needed% &efree inventory spaces to claim a pending playtime reward.").toString()
                .replaceAll("%player%", player)
                .replaceAll("%rewardname%", rewardname)
                .replaceAll("%inventory_spaces_needed%", Integer.toString(inventoryNeeded)));
    }
    //</editor-fold>

    //<editor-fold desc="Commands" defaultstate="collapsed">
    public String getPlaytime(int time) {
        return ChatColor.translateAlternateColorCodes('&', this.timeToString(time, this.messages.getOrDefault("Playtime", "&6You have played for %days% days, %hours% hours, %minutes% minutes and %seconds% seconds on this server.").toString()));
    }

    public String getPlaytimeOther(int time, String player) {
        return ChatColor.translateAlternateColorCodes('&', this.timeToString(time, this.messages.getOrDefault("PlaytimeOther", "&6%player% has played on this server for %days% days, %hours% hours, %minutes% minutes and %seconds% seconds.").toString())
                .replaceAll("%player%", player));
    }

    public String getAFKTime(int time) {
        return ChatColor.translateAlternateColorCodes('&', this.timeToString(time, this.messages.getOrDefault("AFKTime", "&6You have AFK'd for %days% days, %hours% hours, %minutes% minutes and %seconds% seconds on this server.").toString()));
    }

    public String getAFKTimeOther(int time, String player) {
        return ChatColor.translateAlternateColorCodes('&', this.timeToString(time, this.messages.getOrDefault("AFKTimeOther", "&6%player% has AFK'd on this server for %days% days, %hours% hours, %minutes% minutes and %seconds% seconds.").toString())
                .replaceAll("%player%", player));
    }

    public String getRewardGiven(String player, String rewardname) {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("RewardGiven", "&aThe %rewardname% reward has been given to %player%.").toString()
                .replaceAll("%rewardname%", rewardname)
                .replaceAll("%player%", player));
    }
    
    public String getReloadSuccess(){
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("ReloadSuccess", "&aConfig and messages reloaded successfully.").toString());
    }
    
    public String getResetSuccess(String player, String rewardname){
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("ResetSuccess", "&a%rewardname% for player %player% was successfully reset.").toString()
                .replaceAll("%rewardname%", rewardname)
                .replaceAll("%player%", player));
    }
    //</editor-fold>

    //<editor-fold desc="Errors" defaultstate="collapsed">
    public String getNeedToBeOnline() {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("NeedToBeOnline", "&4You will need to log in to use this command.").toString());
    }

    public String getServerDoesntKeepTrackOfAFK() {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("ServerDoesntKeepTrackOfAFK", "&4This server does not keep track of AFK time.").toString());
    }

    public String getNoRewardsConfigured() {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("NoRewardsConfigured", "&4No rewards configured in the config file.").toString());
    }

    public String getMaxInventExceeded(String command) {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("MaxInventExceeded", "&4Max inventory size exceeded, please use &b%command%&4.").toString()
                .replaceAll("%command%", command));
    }

    public String getNoRewardsAvailable() {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("NoRewardsAvailable", "&4There are no rewards available for you.").toString());
    }

    public String getNoPermission() {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("NoPermission", "&4You do not have permission to perform this command.").toString());
    }

    public String getNoData(String player) {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("NoData", "&4%player% has no data on this server yet.").toString()
                .replaceAll("%player%", player));
    }

    public String getNoSuchReward(String rewardname) {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("NoSuchReward", "&4%rewardname% is not an available reward.").toString()
                .replaceAll("%rewardname%", rewardname));
    }

    public String getNotANumber() {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("NotANumber", "&4Amount must be a positive number greater then 0.").toString());
    }

    public String getNotABoolean() {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("NotABoolean", "&4Broadcast must be true or false.").toString());
    }

    public String getNotOnline(String player) {
        return ChatColor.translateAlternateColorCodes('&', this.messages.getOrDefault("NotOnline", "&4The player %player% is not online.").toString()
                .replaceAll("%player%", player));
    }
    //</editor-fold>

    private String timeToString(int time, String message) {
        double totalSeconds = time / 20, totalMinutes = time / 1200, totalHours = time / 72000, totalDays = time / 1728000;
        int playtimeInSeconds = time / 20;
        int days = 0, hours = 0, minutes = 0;
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
        return message.replaceAll("%days%", Integer.toString(days))
                .replaceAll("%hours%", Integer.toString(hours))
                .replaceAll("%minutes%", Integer.toString(minutes))
                .replaceAll("%seconds%", Integer.toString(playtimeInSeconds))
                .replaceAll("%total_in_days%", Double.toString(totalDays))
                .replaceAll("%total_in_hours%", Double.toString(totalHours))
                .replaceAll("%total_in_minutes%", Double.toString(totalMinutes))
                .replaceAll("%total_in_seconds%", Double.toString(totalSeconds));
    }
}
