package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.helperClasses.ConfigReward;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class LitePlaytimeRewardsConfig {

    private final FileConfiguration config;
    private final LitePlaytimeRewards plugin;

    private final TreeMap<String, ConfigReward> rewards = new TreeMap<>();

    public LitePlaytimeRewardsConfig(LitePlaytimeRewards plugin) {
        this.plugin = plugin;
        this.config = this.plugin.getConfig();
    }

    // <editor-fold desc="Miscellaneous" defaultstate="collapsed">
    public boolean isUpdateChecker() {
        return this.config.getBoolean("UpdateChecker", true);
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
            rewardsSection.getKeys(false).forEach(e -> {
                ConfigurationSection rewardConfig = rewardsSection.getConfigurationSection(e);
                ConfigReward reward = this.getRewardFromConfig(rewardConfig);
                if (reward != null) {
                    this.rewards.put(e.toLowerCase(), reward);
                } else {
                    Bukkit.getLogger().severe(String.format("[LitePlaytimeRewards] %s was not loaded because it was configured incorrectly.", e));
                }
            });
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

    private ConfigReward getRewardFromConfig(ConfigurationSection reward) {
        String displayName = null;
        List<Integer> playtimeNeeded = new ArrayList<>();
        boolean countAfkTime = false;
        int slotsNeeded = 0;
        boolean loop = false;
        List<String> disabledWorlds = new ArrayList<>();
        String notificationType = "chat";
        String notification = null;
        String broadcastNotification = null;
        List<String> commands = null;

        if (reward.contains("DisplayName")) {
            displayName = reward.get("DisplayName").toString();
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
        if (reward.contains("NotificationType")) {
            notificationType = reward.get("NotificationType").toString();
        }
        if (reward.contains("Notification")) {
            notification = reward.get("Notification").toString();
        }
        if (reward.contains("BroadcastNotification")) {
            broadcastNotification = reward.get("BroadcastNotification").toString();
        }
        if (reward.contains("Commands")) {
            commands = (List<String>) reward.get("Commands");
        }

        if (displayName == null || playtimeNeeded.isEmpty() || slotsNeeded < 0 || notification == null || broadcastNotification == null || commands == null) {
            return null;
        }

        return new ConfigReward(displayName, playtimeNeeded, countAfkTime, slotsNeeded, loop, disabledWorlds, notificationType, notification.replaceAll("&", "§"), broadcastNotification.replaceAll("&", "§"), commands);
    }

    private List<Integer> getNumbersFromString(String numbers) {
        try {
            return Arrays.asList(numbers.split(",")).stream().map(e -> Integer.parseInt(e)).collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }
}
