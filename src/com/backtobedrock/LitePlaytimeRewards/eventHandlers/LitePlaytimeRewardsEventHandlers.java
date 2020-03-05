package com.backtobedrock.LitePlaytimeRewards.eventHandlers;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsCRUD;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsConfig;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.Reward;
import com.backtobedrock.LitePlaytimeRewards.runnables.Countdown;
import java.util.Map;
import java.util.TreeMap;
import static java.util.stream.Collectors.toMap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

public class LitePlaytimeRewardsEventHandlers implements Listener {

    private final LitePlaytimeRewards plugin;
    private final LitePlaytimeRewardsConfig config;

    public LitePlaytimeRewardsEventHandlers(LitePlaytimeRewards plugin) {
        this.plugin = plugin;
        this.config = this.plugin.getLPRConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        LitePlaytimeRewardsCRUD crud = new LitePlaytimeRewardsCRUD(this.plugin, e.getPlayer());
        TreeMap<String, Reward> redeemed = crud.getRewards().entrySet().stream().filter(r -> r.getValue().getTimeTillNextReward().get(0) != -1).collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, TreeMap::new));
        BukkitTask rewardRunnable = new Countdown(this.plugin, e.getPlayer(), redeemed, this.config.getRewards()).runTaskTimer(this.plugin, 1200, 1200);
        this.plugin.addToRunningRewards(e.getPlayer().getUniqueId(), rewardRunnable.getTaskId());
        if (this.config.isUpdateChecker() && e.getPlayer().isOp()) {
            this.plugin.checkForOldVersion();
            if (this.plugin.isOldVersion()) {
                e.getPlayer().spigot().sendMessage(new ComponentBuilder("There is a new version of LitePlaytimeRewards available ").color(ChatColor.YELLOW).append("here").color(ChatColor.AQUA).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/liteplaytimerewards-give-rewards-based-on-playtime-with-ease.71784/history")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Go to spigotmc.org").create())).append(".").color(ChatColor.YELLOW).create());
            }
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        Bukkit.getScheduler().cancelTask(this.plugin.getFromRunningRewards(e.getPlayer().getUniqueId()));
        this.plugin.removeFromRunningRewards(e.getPlayer().getUniqueId());
    }
}
