package com.backtobedrock.rewardslite.runnables;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.domain.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class Playtime extends BukkitRunnable {
    private final Rewardslite plugin;
    private final Player player;
    private final PlayerData playerData;
    private final List<RewardData> rewards;

    //time count
    private int tickCount = 0;
    private int playtimeTickCount = 0;
    private int autoSaveCount = 0;

    public Playtime(Player player, PlayerData playerData) {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.player = player;
        this.playerData = playerData;
        this.rewards = this.playerData.getRewards();
    }

    public void start() {
        this.runTaskTimer(this.plugin, 0, 1);
    }

    public void stop() {
        this.cancel();
    }

    @Override
    public void run() {
        if (this.playerData.isAfk()) {
            this.playerData.increaseAfkTime(1);
        } else {
            this.playerData.increasePlaytime(1);
            this.playtimeTickCount++;
        }

        this.tickCount++;
        if (this.tickCount == 20) {
            boolean alteredRewards = false;
            for (RewardData rewardData : this.rewards) {
                if (!rewardData.hasCountedPrevious()) {
                    rewardData.countPrevious(this.player, this.playerData);
                    continue;
                }

                if (this.player.getLocation().getWorld() != null && rewardData.getDisableGainingPlaytimeInWorlds().contains(this.player.getLocation().getWorld().getName().toLowerCase())) {
                    continue;
                }

                //countdown
                if (!rewardData.isCountAfk()) {
                    alteredRewards = rewardData.decreaseTimeLeft(this.playtimeTickCount, this.player, false) || alteredRewards;
                } else {
                    alteredRewards = rewardData.decreaseTimeLeft(this.tickCount, this.player, false) || alteredRewards;
                }
            }
            this.tickCount = 0;
            this.playtimeTickCount = 0;

            if (alteredRewards) {
                this.plugin.getPlayerRepository().updatePlayerData(this.playerData);
            }
        }

        this.autoSaveCount++;
        if (this.autoSaveCount == 6000) {
            this.autoSaveCount = 0;
            this.plugin.getPlayerRepository().updatePlayerData(this.playerData);
        }
    }
}
