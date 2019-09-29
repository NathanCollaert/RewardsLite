package com.backtobedrock.LitePlaytimeRewards.eventHandlers;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsCRUD;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsConfig;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
        this.plugin.addToOnlinePlayerListLastPlaytimeCheck(e.getPlayer().getUniqueId(), crud.getRedeemedRewards());
        if (this.config.isCheckAvailableRewardsOnPlayerJoin() && !this.plugin.getLPRConfig().getRewards().isEmpty()) {
            this.plugin.checkEligibleForRewards(e.getPlayer());
        }
        if (this.config.isUpdateChecker() && e.getPlayer().isOp() && this.plugin.isOldVersion()) {
            e.getPlayer().spigot().sendMessage(new ComponentBuilder("There is a new version of LitePlaytimeRewards available ").color(ChatColor.YELLOW).append("here").color(ChatColor.AQUA).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Go to spigotmc.org").create())).append(".").color(ChatColor.YELLOW).create());
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        this.plugin.removeFromOnlinePlayerListLastPlaytimeCheck(e.getPlayer().getUniqueId());
    }
}
