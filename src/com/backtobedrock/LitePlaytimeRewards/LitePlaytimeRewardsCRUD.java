package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.helperClasses.Reward;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public final class LitePlaytimeRewardsCRUD {

    private final LitePlaytimeRewards plugin;

    private File file = null;
    private FileConfiguration configuration;
    private final OfflinePlayer player;

    private TreeMap<String, Reward> rewards = new TreeMap<>();
    private long playtime;
    private long afktime;

    public LitePlaytimeRewardsCRUD(OfflinePlayer player) {
        this.plugin = LitePlaytimeRewards.getInstance();
        this.player = player;

        //initialize data with data from file
        this.getData();
    }

    private void setNewStart() {
        FileConfiguration conf = this.getConfig();

        conf.set("uuid", player.getUniqueId().toString());
        conf.set("playername", player.getName());
        conf.set("playtime", 0);
        conf.set("afktime", 0);
        conf.set("rewards", new TreeMap<>());

        this.saveConfig();
    }

    private void getData() {
        FileConfiguration conf = this.getConfig();

        this.playtime = conf.getInt("playtime");
        this.afktime = conf.getInt("afktime");

        //get rewards and check if config has untracked rewards
        ConfigurationSection rewardsSection = conf.getConfigurationSection("rewards");
        Set<String> keys = rewardsSection != null ? rewardsSection.getKeys(false) : new HashSet<>();
        this.plugin.getLPRConfig().getRewards().entrySet().stream().forEach(f -> {
            if (keys.contains(f.getKey())) {
                Reward reward = rewardsSection.getSerializable(f.getKey(), Reward.class);
                if (reward != null) {
                    this.rewards.put(f.getKey(), new Reward(f.getValue(), reward.getTimeTillNextReward(), reward.getAmountRedeemed(), reward.getAmountPending()));
                }
            } else {
                this.rewards.put(f.getKey(), new Reward(f.getValue()));
            }
        });
        conf.set("rewards", this.rewards);

        conf.set("playername", player.getName());

        this.saveConfig();
    }

    public void setRewards(TreeMap<String, Reward> rewards, boolean save) {
        FileConfiguration conf = this.getConfig();
        conf.set("rewards", rewards);
        this.rewards = rewards;
        if (save) {
            this.saveConfig();
        }
    }

    public void replaceRewards(TreeMap<String, Reward> rewards, boolean save) {
        FileConfiguration conf = this.getConfig();
        rewards.entrySet().forEach(e -> this.rewards.replace(e.getKey().toLowerCase(), e.getValue()));
        conf.set("rewards", this.rewards);
        if (save) {
            this.saveConfig();
        }
    }

    public TreeMap<String, Reward> getRewards() {
        return this.rewards;
    }

    public long getPlaytime() {
        return this.playtime;
    }

    public void setPlaytime(long playtime, boolean save) {
        FileConfiguration conf = this.getConfig();
        conf.set("playtime", playtime);
        this.playtime = playtime;
        if (save) {
            this.saveConfig();
        }
    }

    public long getAfktime() {
        return this.afktime;
    }

    public void setAfktime(long afktime, boolean save) {
        FileConfiguration conf = this.getConfig();
        conf.set("afktime", afktime);
        this.afktime = afktime;
        if (save) {
            this.saveConfig();
        }
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
                        Bukkit.getLogger().log(Level.INFO, "[LPR] File for player {0} has been created", player.getName());
                    }
                } catch (IOException e) {
                    Bukkit.getLogger().log(Level.SEVERE, "[LPR] Cannot create data for {0}", player.getName());
                }
            }
            return file;
        }
        return file;
    }

    public static boolean doesPlayerDataExists(OfflinePlayer plyr) {
        File file = new File(LitePlaytimeRewards.getInstance().getDataFolder() + "/userdata/" + plyr.getUniqueId().toString() + ".yml");
        return file.exists();
    }
}
