package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.helperClasses.ConfigReward;
import java.util.List;
import java.util.TreeMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class LitePlaytimeRewardsConfig {

    private final FileConfiguration config;

    private final LitePlaytimeRewards plugin;

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
    public boolean isCheckAvailableRewardsOnPlayerJoin() {
        return this.config.getBoolean("CheckAvailableRewardsOnPlayerJoin", true);
    }

    public int getRewardCheck() {
        return this.checkMin(this.config.getInt("RewardCheck", 60), 1, 60);
    }

    public List<String> getDisableGettingRewardsInWorlds() {
        return this.config.getStringList("DisableGettingRewardsInWorlds");
    }

    public boolean isCountPlaytimeFromStart() {
        return this.config.getBoolean("CountPlaytimeFromStart", false);
    }
    // </editor-fold>

    // <editor-fold desc="Rewards" defaultstate="collapsed">
    public TreeMap<String, ConfigReward> getRewards() {
        TreeMap<String, ConfigReward> rewards = new TreeMap<>();
        ConfigurationSection rewardsSection = this.config.getConfigurationSection("Rewards");
        rewardsSection.getKeys(false).forEach(e -> {
            ConfigurationSection reward = rewardsSection.getConfigurationSection(e);
            String displayName = reward.getString("DisplayName");
            int playtimeNeeded = reward.getInt("PlaytimeNeeded");
            boolean countPlaytimeFromStart = reward.getBoolean("CountPlaytimeFromStart");
            boolean loop = reward.getBoolean("Loop");
            String notification = reward.getString("Notification").replaceAll("&", "§");
            String broadcastNotification = reward.getString("BroadcastNotification").replaceAll("&", "§");
            List<String> commands = reward.getStringList("Commands");
            if (displayName == null || displayName.isEmpty() || playtimeNeeded == 0 || notification == null || broadcastNotification == null || commands.isEmpty()) {
                this.plugin.getLogger().severe(String.format("The reward %s has not been added due to it not being configured correctly.", e));
            } else {
                rewards.put(e, new ConfigReward(displayName, playtimeNeeded, countPlaytimeFromStart, loop, notification, broadcastNotification, commands));
            }
        });
        return rewards;
    }
    // </editor-fold>

    private int checkMin(int value, int min, int defaultValue) {
        if (value >= min) {
            return value;
        } else {
            return defaultValue;
        }
    }
}
