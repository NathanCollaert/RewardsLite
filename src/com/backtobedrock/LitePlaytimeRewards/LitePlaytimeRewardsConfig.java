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
import org.bukkit.plugin.java.JavaPlugin;

public class LitePlaytimeRewardsConfig {

    private FileConfiguration config;
    private final LitePlaytimeRewards plugin;

    private TreeMap<String, ConfigReward> rewards = new TreeMap<>();

    public LitePlaytimeRewardsConfig() {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        this.config = this.plugin.getConfig();
    }

    public void setConfig(FileConfiguration fc) {
        this.config = fc;
        this.rewards.clear();
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
    public boolean isCountAllPlaytime() {
        return this.config.getBoolean("CountAllPlaytime", true);
    }

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

    private ConfigReward getRewardFromConfig(String name, ConfigurationSection reward) {
        String displayName = ChatColor.translateAlternateColorCodes('&', reward.getString("DisplayName", name));
        Material displayItem = Material.CHEST;
        List<String> displayDescription = reward.getStringList("DisplayDescription").stream().map(e -> ChatColor.translateAlternateColorCodes('&', e)).collect(Collectors.toList());
        List<Integer> playtimeNeeded = this.getNumbersFromString(reward.getString("PlaytimeNeeded", ""));
        boolean countAfkTime = reward.getBoolean("CountAfkTime", true);
        int slotsNeeded = this.checkMin(reward.getInt("SlotsNeeded", 0), 0, 0);
        boolean loop = reward.getBoolean("Loop", false);
        List<String> disabledWorlds = reward.getStringList("DisabledWorlds");
        boolean usePermission = reward.getBoolean("UsePermission", false);
        String notificationType = reward.getString("NotificationType", "bossbar");
        String notification = ChatColor.translateAlternateColorCodes('&', reward.getString("Notification", ""));
        String broadcastNotification = ChatColor.translateAlternateColorCodes('&', reward.getString("BroadcastNotification", ""));
        List<String> commands = reward.getStringList("Commands");

        Material m = Material.matchMaterial(reward.getString("DisplayItem", "chest"));
        if (m != null && m != Material.AIR) {
            displayItem = m;
        }

        if (playtimeNeeded.isEmpty() || slotsNeeded < 0 || commands.isEmpty()) {
            return null;
        }

        return new ConfigReward(displayName, displayItem, displayDescription, playtimeNeeded, countAfkTime, slotsNeeded, loop, disabledWorlds, usePermission, notificationType, notification.replaceAll("&", "§"), broadcastNotification.replaceAll("&", "§"), commands);
    }

    private List<Integer> getNumbersFromString(String numbers) {
        try {
            return Arrays.asList(numbers.split(",")).stream().map(e -> (Integer.parseInt(e) > 1789569 ? 1789569 : Integer.parseInt(e)) * 1200).collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }

    private int checkMin(int value, int min, int defaultValue) {
        if (value >= min) {
            return value;
        } else {
            return defaultValue;
        }
    }
}
