package com.backtobedrock.LitePlaytimeRewards.configs;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.models.ConfigReward;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Rewards {

    private final File rewardFile;
    private FileConfiguration config;
    private final LitePlaytimeRewards plugin;

    private TreeMap<String, ConfigReward> rewards;

    public Rewards(File rewardFile) {
        this.rewardFile = rewardFile;
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        this.setConfig(YamlConfiguration.loadConfiguration(rewardFile));
    }

    public void setConfig(FileConfiguration fc) {
        this.config = fc;
        this.rewards = null;
        this.plugin.getLogger().log(Level.INFO, "Loaded {0} rewards.", this.getAll().size());
    }

    public TreeMap<String, ConfigReward> getAll() {
        if (this.rewards == null) {
            this.rewards = new TreeMap<>();
            ConfigurationSection rewardsSection = this.config.getConfigurationSection("Rewards");
            if (rewardsSection != null) {
                rewardsSection.getKeys(false).forEach(e -> {
                    ConfigurationSection rewardConfig = rewardsSection.getConfigurationSection(e);
                    ConfigReward reward = ConfigReward.deserialize(e, rewardConfig);
                    if (reward != null) {
                        this.rewards.put(e.toLowerCase(), reward);
                    }
                });
            }
        }
        return this.rewards;
    }

    public boolean doesRewardExist(String id) {
        return this.getAll().containsKey(id.toLowerCase());
    }

    public void saveRewards() {
        TreeMap<String, Map<String, Object>> crewards = new TreeMap<>();
        this.getAll().entrySet().stream().forEach(e -> {
            crewards.put(e.getKey(), e.getValue().serialize());
        });
        this.config.set("Rewards", crewards);

        this.saveConfig();
    }

    private void saveConfig() {
        try {
            this.config.save(this.rewardFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot save to {0}", this.rewardFile.getName());
        }
    }
}
