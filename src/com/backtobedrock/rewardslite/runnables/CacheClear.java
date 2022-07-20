package com.backtobedrock.rewardslite.runnables;

import com.backtobedrock.rewardslite.Rewardslite;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CacheClear extends BukkitRunnable {
    private final Rewardslite plugin;
    private final OfflinePlayer player;
    private final int delay;

    public CacheClear(OfflinePlayer player, int delay) {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.player = player;
        this.delay = delay;
    }

    public void start() {
        this.runTaskLaterAsynchronously(this.plugin, this.delay);
    }

    public void stop() {
        this.cancel();
    }

    @Override
    public void run() {
        if (!this.player.isOnline()) {
            this.plugin.getPlayerRepository().removeFromPlayerCache(this.player.getUniqueId());
        }
    }
}
