package com.backtobedrock.LitePlaytimeRewards.runnables;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;

public class NotifyBossBar extends BukkitRunnable {
    private final BossBar bar;
    private int counter = 10;

    public NotifyBossBar(Collection<Player> players, String notification, BarColor color) {
        this.bar = Bukkit.createBossBar(notification, color, BarStyle.SOLID);
        players.forEach(this.bar::addPlayer);
        this.bar.setProgress(1);
        this.bar.setVisible(true);
    }

    @Override
    public void run() {
        if (this.counter > 0) {
            this.bar.setProgress((double) 1 / 10 * this.counter);
            this.counter--;
        } else {
            this.bar.setVisible(false);
            this.cancel();
        }
    }

}
