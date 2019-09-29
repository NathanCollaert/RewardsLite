package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.helperClasses.RedeemedReward;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class LitePlaytimeRewardsCRUD {

    private final LitePlaytimeRewards plugin;

    private File file = null;
    private FileConfiguration configuration;
    private final OfflinePlayer player;
    private TreeMap<String, RedeemedReward> rewards;
    private final long playtimeStart;

    public LitePlaytimeRewardsCRUD(LitePlaytimeRewards plugin, OfflinePlayer player) {
        this.plugin = plugin;
        this.player = player;
        this.playtimeStart = this.getConfig().getLong("playtimeStart");
        ConfigurationSection rewardsSection = this.getConfig().getConfigurationSection("rewards");
        rewardsSection.getKeys(false).forEach(e -> {
            this.rewards.put(e, rewardsSection.getObject(e, RedeemedReward.class));
        });
    }

    private void setNewStart() {
        FileConfiguration conf = this.getConfig();
        conf.set("uuid", player.getUniqueId().toString());
        conf.set("playername", player.getName());
        conf.set("playtimeStart", player.getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE));
        this.saveConfig();
    }

    public void setRewards(TreeMap<String, RedeemedReward> rewards, boolean save) {
        FileConfiguration conf = this.getConfig();
        conf.set("playername", player.getName());
        conf.set("rewards", rewards);
        this.rewards = rewards;
        if (save) {
            this.saveConfig();
        }
    }

    public TreeMap<String, RedeemedReward> getRewards() {
        return this.rewards;
    }

    public long getPlaytimeStart() {
        return playtimeStart;
    }

    public FileConfiguration getConfig() {
        if (configuration == null) {
            configuration = YamlConfiguration.loadConfiguration(getFile());
            return configuration;
        }
        return configuration;
    }

    public void saveConfig() {
        try {
            configuration.save(this.getFile());
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot save to {0}", file.getName());
        }
    }

    private File getFile() {
        if (file == null) {
            this.file = new File(this.plugin.getDataFolder() + "/userdata/" + player.getUniqueId().toString() + ".yml");
            if (!this.file.exists()) {
                try {
                    if (this.file.createNewFile()) {
                        this.setNewStart();
                        Bukkit.getLogger().log(Level.INFO, "[LitePlaytimeRewards] File for player {0} has been created", player.getName());
                    }
                } catch (IOException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "[LitePlaytimeRewards] Cannot create data for {0}", player.getName());
                }
            }
            return file;
        }
        return file;
    }
}
