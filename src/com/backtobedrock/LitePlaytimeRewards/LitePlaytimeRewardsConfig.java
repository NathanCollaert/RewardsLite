package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.helperClasses.ConfigReward;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class LitePlaytimeRewardsConfig {

    private final FileConfiguration config;
    private final LitePlaytimeRewards plugin;

    private final TreeMap<String, ConfigReward> rewards = new TreeMap<>();

    public LitePlaytimeRewardsConfig() {
        this.plugin = LitePlaytimeRewards.getInstance();
        this.config = this.plugin.getConfig();
    }

    // <editor-fold desc="Miscellaneous" defaultstate="collapsed">
    public boolean isUpdateChecker() {
        return this.config.getBoolean("UpdateChecker", true);
    }
    
    public boolean isUsebStats() {
        return this.config.getBoolean("bStats", true);
    }

    public int getAutoSave() {
        return this.checkMin(this.config.getInt("AutoSave", 1), 1, 1) * 1200;
    }

    public int getTimeKeepDataInCache() {
        return this.checkMin(this.config.getInt("TimeKeepDataInCache", 5), 0, 5) * 1200;
    }
    // </editor-fold>

    // <editor-fold desc="Playtime Options" defaultstate="collapsed">
    public List<String> getDisableGettingRewardsInWorlds() {
        return this.config.getStringList("DisableGettingRewardsInWorlds").stream().map(String::toLowerCase).collect(Collectors.toList());
    }
    // </editor-fold>

    // <editor-fold desc="Rewards" defaultstate="collapsed">
    public TreeMap<String, ConfigReward> getRewards() {
        if (this.rewards.isEmpty()) {
            ConfigurationSection rewardsSection = this.config.getConfigurationSection("Rewards");
            if (rewardsSection != null) {
                rewardsSection.getKeys(false).forEach(e -> {
                    ConfigurationSection rewardConfig = rewardsSection.getConfigurationSection(e);
                    ConfigReward reward = this.getRewardFromConfig(e, rewardConfig);
                    if (reward != null) {
                        this.rewards.put(e.toLowerCase(), reward);
                    } else {
                        Bukkit.getLogger().severe(String.format("[LPR] %s was not loaded because it was configured incorrectly.", e));
                    }
                });
            }
        }
        return this.rewards;
    }
    // </editor-fold>

    private int checkMin(int value, int min, int defaultValue) {
        if (value >= min) {
            return value;
        } else {
            return defaultValue;
        }
    }

    private ConfigReward getRewardFromConfig(String name, ConfigurationSection reward) {
        String displayName = name;
        Material displayItem = Material.CHEST;
        List<String> displayDescription = new ArrayList<>();
        List<Long> playtimeNeeded = new ArrayList<>();
        boolean countAfkTime = true;
        int slotsNeeded = 0;
        boolean loop = false;
        List<String> disabledWorlds = new ArrayList<>();
        boolean usePermission = false;
        String notificationType = "chat";
        String notification = "";
        String broadcastNotification = "";
        List<String> commands = null;

        if (reward.contains("DisplayName")) {
            displayName = ChatColor.translateAlternateColorCodes('&', reward.get("DisplayName").toString());
        }
        if (reward.contains("DisplayItem")) {
            Material m = Material.matchMaterial(reward.get("DisplayItem").toString());
            if (m != null && m != Material.AIR) {
                displayItem = m;
            }
        }
        if (reward.contains("DisplayDescription")) {
            List<String> desc = (List<String>) reward.get("DisplayDescription");
            if (desc != null) {
                desc.replaceAll(e -> ChatColor.translateAlternateColorCodes('&', e));
                displayDescription = desc;
            }
        }
        if (reward.contains("PlaytimeNeeded")) {
            playtimeNeeded = this.getNumbersFromString(reward.get("PlaytimeNeeded").toString());
        }
        if (reward.contains("CountAfkTime")) {
            countAfkTime = (boolean) reward.get("CountAfkTime");
        }
        if (reward.contains("SlotsNeeded")) {
            slotsNeeded = (int) reward.get("SlotsNeeded");
        }
        if (reward.contains("Loop")) {
            loop = (boolean) reward.get("Loop");
        }
        if (reward.contains("DisabledWorlds")) {
            disabledWorlds = (List<String>) reward.get("DisabledWorlds");
        }
        if (reward.contains("UsePermission")) {
            usePermission = (boolean) reward.get("UsePermission");
        }
        if (reward.contains("NotificationType")) {
            notificationType = reward.get("NotificationType").toString();
        }
        if (reward.contains("Notification")) {
            notification = ChatColor.translateAlternateColorCodes('&', reward.get("Notification").toString());
        }
        if (reward.contains("BroadcastNotification")) {
            broadcastNotification = ChatColor.translateAlternateColorCodes('&', reward.get("BroadcastNotification").toString());
        }
        if (reward.contains("Commands")) {
            commands = (List<String>) reward.get("Commands");
        }

        if (playtimeNeeded.isEmpty() || slotsNeeded < 0 || commands == null) {
            return null;
        }

        return new ConfigReward(displayName, displayItem, displayDescription, playtimeNeeded, countAfkTime, slotsNeeded, loop, disabledWorlds, usePermission, notificationType, notification.replaceAll("&", "§"), broadcastNotification.replaceAll("&", "§"), commands);
    }

    private List<Long> getNumbersFromString(String numbers) {
        try {
            return Arrays.asList(numbers.split(",")).stream().map(e -> Long.parseLong(e) * 1200).collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }
}
