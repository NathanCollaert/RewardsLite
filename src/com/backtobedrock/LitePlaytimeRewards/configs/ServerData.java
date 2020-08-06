package com.backtobedrock.LitePlaytimeRewards.configs;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ServerData {

    private final File serverFile;
    private FileConfiguration config;
    private final LitePlaytimeRewards plugin;

    private int totalPlaytime = -1;
    private TreeMap<UUID, Integer> topPlayers = null;

    public ServerData(File serverFile) {
        this.serverFile = serverFile;
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        this.setConfig(YamlConfiguration.loadConfiguration(serverFile));
    }

    public void setConfig(FileConfiguration fc) {
        this.config = fc;
    }

    public TreeMap<UUID, Integer> getTopPlayers() {
        if (this.topPlayers == null) {
            this.topPlayers = new TreeMap<>();
            ConfigurationSection topSection = this.config.getConfigurationSection("top_players");
            if (topSection != null) {
                topSection.getKeys(false).forEach(e -> {
                    this.topPlayers.put(UUID.fromString(e), topSection.getInt(e));
                });
            }
            this.config.set("top_players", this.topPlayers);
        }
        return this.topPlayers;
    }

    public int getTotalPlaytime() {
        if (this.totalPlaytime == -1) {
            this.totalPlaytime = this.config.getInt("total_playtime");
        }
        return this.totalPlaytime;
    }

    private void saveConfig() {
        try {
            this.config.save(this.serverFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot save to {0}", this.serverFile.getName());
        }
    }
}
