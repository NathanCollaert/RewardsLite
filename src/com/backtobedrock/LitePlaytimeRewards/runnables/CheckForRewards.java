package com.backtobedrock.LitePlaytimeRewards.runnables;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import org.bukkit.scheduler.BukkitRunnable;

public class CheckForRewards extends BukkitRunnable {

    private final LitePlaytimeRewards plugin;

    public CheckForRewards(LitePlaytimeRewards plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        this.plugin.getServer().getOnlinePlayers().stream().forEach(p -> {
            this.plugin.checkEligibleForRewards(p);
        });
    }

}
