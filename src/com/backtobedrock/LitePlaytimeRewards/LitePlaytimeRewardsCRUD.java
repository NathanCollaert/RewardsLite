package com.backtobedrock.LitePlaytimeRewards;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LitePlaytimeRewardsCRUD {

    private final LitePlaytimeRewards plugin;

    private File file = null;
    private FileConfiguration configuration;
    private final OfflinePlayer player;
    private TreeMap<String, Long> rewards = new TreeMap<>();

    public LitePlaytimeRewardsCRUD(LitePlaytimeRewards plugin, OfflinePlayer player) {
        this.plugin = plugin;
        this.player = player;
        //get all rewards writen away and cast them to long
        this.getConfig().getConfigurationSection("rewards").getValues(false).entrySet().forEach(e -> {
            this.rewards.put(e.getKey(), Long.parseLong(String.valueOf(e.getValue())));
        });
    }

    private void setNewStart() {
        FileConfiguration conf = this.getConfig();
        TreeMap<String, Long> rewardsPH = new TreeMap<>();
        conf.set("uuid", player.getUniqueId().toString());
        conf.set("playername", player.getName());
        //get all rewards and check if from count start or not
        this.plugin.getLPRConfig().getRewards().entrySet().forEach(e -> {
            rewardsPH.put(e.getKey(), e.getValue().isCountPlaytimeFromStart() ? (long) 0 : player.getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE));
        });
        conf.set("rewards", rewardsPH);
        this.saveConfig();
    }

    public void setRewards(TreeMap<String, Long> rewards, boolean save) {
        FileConfiguration conf = this.getConfig();
        conf.set("playername", player.getName());
        conf.set("rewards", rewards);
        this.rewards = rewards;
        if (save) {
            this.saveConfig();
        }
    }

    public TreeMap<String, Long> getRewards() {
        return this.rewards;
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
