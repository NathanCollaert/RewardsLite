package com.backtobedrock.LitePlaytimeRewards.configs;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.models.Reward;
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
import org.bukkit.plugin.java.JavaPlugin;

public final class PlayerData {

    private final LitePlaytimeRewards plugin;

    private File file = null;
    private FileConfiguration configuration;
    private final OfflinePlayer player;

    private final TreeMap<String, Reward> rewards = new TreeMap<>();
    private int playtime;
    private int afktime;

    public PlayerData(OfflinePlayer player) {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        this.player = player;

        //initialize data with data from file
        this.getData();
    }

    private void setNewStart() {
        FileConfiguration conf = this.getConfig();

        conf.set("uuid", this.player.getUniqueId().toString());
        conf.set("playername", this.player.getName());
        conf.set("playtime", this.plugin.getLPRConfig().isCountAllPlaytime() ? new Long(this.player.getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE)) : 0);
        conf.set("afktime", 0);
        conf.set("rewards", new TreeMap<>());

        this.saveConfig();
    }

    public void getData() {
        //clear rewards and conf for reload
        this.rewards.clear();
        this.configuration = null;

        FileConfiguration conf = this.getConfig();

        this.playtime = conf.getInt("playtime", 0);
        this.afktime = conf.getInt("afktime", 0);

        //get rewards and check if config has untracked rewards
        ConfigurationSection rewardsSection = conf.getConfigurationSection("rewards");
        this.plugin.getRewards().getAll().entrySet().stream().forEach(f -> {
            Reward reward;
            if (rewardsSection != null && rewardsSection.contains(f.getKey())) {
                Reward incompleteReward = (Reward) rewardsSection.get(f.getKey());
                reward = new Reward(f.getValue(), incompleteReward.getTimeTillNextReward(), incompleteReward.getAmountRedeemed(), incompleteReward.getAmountPending(), incompleteReward.isEligible(), incompleteReward.isClaimedOldPlaytime());
            } else {
                reward = new Reward(f.getValue());
            }
            this.rewards.put(f.getKey(), reward);

            this.checkOldPlaytime(reward);
        });

        conf.set("rewards", this.rewards);
        conf.set("playername", this.player.getName());

        this.saveConfig();
    }

    public TreeMap<String, Reward> getRewards() {
        return this.rewards;
    }

    public int getPlaytime() {
        return this.playtime;
    }

    public void setPlaytime(int playtime, boolean save) {
        FileConfiguration conf = this.getConfig();
        conf.set("playtime", playtime);
        this.playtime = playtime;
        if (save) {
            this.saveConfig();
        }
    }

    public int getAfktime() {
        return this.afktime;
    }

    public void setAfktime(int afktime, boolean save) {
        FileConfiguration conf = this.getConfig();
        conf.set("afktime", afktime);
        this.afktime = afktime;
        if (save) {
            this.saveConfig();
        }
    }

    public OfflinePlayer getPlayer() {
        return this.player;
    }

    private void checkOldPlaytime(Reward reward) {
        if (!reward.getcReward().isCountAllPlaytime()) {
            return;
        }

        if (reward.isClaimedOldPlaytime()) {
            return;
        }

        if (this.player.getPlayer() == null) {
            return;
        }

        if (!reward.hasPermission(this.player.getPlayer())) {
            return;
        }

        int total = 0;
        int playtimeCopy = reward.getcReward().isCountAfkTime() ? this.playtime : Math.max(this.playtime - this.afktime, 0);
        reward.resetTimeTillNextReward();
        while (reward.isEligible() && playtimeCopy > reward.getTimeTillNextReward().get(0)) {
            total += 1;
            playtimeCopy -= reward.getTimeTillNextReward().get(0);
            reward.removeFirstTimeTillNextReward();
        }
        if (reward.isEligible()) {
            reward.setFirstTimeTillNextReward(reward.getTimeTillNextReward().get(0) - playtimeCopy);
        }
        reward.setAmountPending(Math.max(total - reward.getAmountRedeemed(), 0));
        reward.setClaimedOldPlaytime(true);
    }

    public FileConfiguration getConfig() {
        if (this.configuration == null) {
            this.configuration = YamlConfiguration.loadConfiguration(getFile());
        }
        return this.configuration;
    }

    public void saveConfig() {
        try {
            FileConfiguration conf = this.getConfig();
            conf.save(this.getFile());
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot save to {0}", this.file.getName());
        }
    }

    private File getFile() {
        if (this.file == null) {
            this.file = new File(this.plugin.getDataFolder() + "/userdata/" + this.player.getUniqueId().toString() + ".yml");
            if (!this.file.exists()) {
                try {
                    if (this.file.createNewFile()) {
                        this.setNewStart();
                        this.plugin.getLogger().log(Level.INFO, "File for player {0} has been created at {1}.", new Object[]{this.player.getName(), this.file.getPath()});
                    }
                } catch (IOException e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Cannot create data for {0}.", this.player.getName());
                }
            }
            return this.file;
        }
        return this.file;
    }

    public static boolean doesPlayerDataExists(OfflinePlayer plyr) {
        File file = new File(JavaPlugin.getPlugin(LitePlaytimeRewards.class).getDataFolder() + "/userdata/" + plyr.getUniqueId().toString() + ".yml");
        return file.exists();
    }
}
