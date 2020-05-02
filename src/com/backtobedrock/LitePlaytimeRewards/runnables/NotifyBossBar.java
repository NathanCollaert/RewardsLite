package com.backtobedrock.LitePlaytimeRewards.runnables;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class NotifyBossBar extends BukkitRunnable {

    private final LitePlaytimeRewards plugin;

    private int counter = 10;
    private final int TOTAL = 10;
    private final BossBar bar;

    public NotifyBossBar(Collection<?> players, String notification, BarColor color) {
        this.plugin = LitePlaytimeRewards.getInstance();
        this.bar = Bukkit.createBossBar(notification, color, BarStyle.SOLID);
        players.stream().forEach(e -> {
            bar.addPlayer((Player) e);
        });
        bar.setProgress(1);
        bar.setVisible(true);
    }

    @Override
    public void run() {
        if (this.counter > 0) {
            this.bar.setProgress((double) 1 / this.TOTAL * this.counter);
            this.counter--;
        } else {
            this.bar.setVisible(false);
            this.cancel();
        }
    }

}
