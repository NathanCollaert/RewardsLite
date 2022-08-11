package com.backtobedrock.rewardslite.runnables;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.notifications.BossBarNotification;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class BossBarNotificationTask extends BukkitRunnable {
    private final Rewardslite plugin;
    private final Player player;
    private final BossBar bossBar;
    private final int time;
    private int timer;

    public BossBarNotificationTask(Player player, String playerName, BossBarNotification bossBarNotification) {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.player = player;
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("player", playerName);
        }};
        this.bossBar = Bukkit.createBossBar(MessageUtils.replacePlaceholders(bossBarNotification.getText(), placeholders), bossBarNotification.getColor(), bossBarNotification.getStyle());
        this.time = 5 * 20;
        this.timer = this.time;
    }

    public void start() {
        this.bossBar.addPlayer(this.player);
        this.bossBar.setVisible(true);
        this.runTaskTimerAsynchronously(this.plugin, 0, 20);
    }

    @Override
    public void run() {
        this.bossBar.setProgress((double) 1 / this.time * this.timer);
        this.timer -= 20;
        if (this.timer < 0) {
            this.bossBar.removePlayer(this.player);
            this.bossBar.setVisible(false);
            this.cancel();
        }
    }
}
