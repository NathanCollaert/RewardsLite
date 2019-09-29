package com.backtobedrock.LitePlaytimeRewards;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
    private TreeMap<String, Long> redeemedRewards;

    public LitePlaytimeRewardsCRUD(LitePlaytimeRewards plugin, OfflinePlayer player) {
        this.plugin = plugin;
        this.player = player;
        this.redeemedRewards = this.getConfig().getObject("redeemedRewards", TreeMap.class, new TreeMap<>());
    }

    private void setNewStart() {
        FileConfiguration conf = this.getConfig();
        conf.set("uuid", player.getUniqueId().toString());
        conf.set("playername", player.getName());
        this.saveConfig();
    }

    public void setRedeemedRewards(TreeMap<String, Long> redeemedRewards, boolean save) {
        FileConfiguration conf = this.getConfig();
        conf.set("playername", player.getName());
        conf.set("redeemedRewards", redeemedRewards);
        this.redeemedRewards = redeemedRewards;
        if (save) {
            this.saveConfig();
        }
    }

    public TreeMap<String, Long> getRedeemedRewards() {
        System.out.println(this.redeemedRewards.values());
        System.out.println(this.redeemedRewards.keySet());
        return this.redeemedRewards;
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
