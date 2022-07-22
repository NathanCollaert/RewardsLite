package com.backtobedrock.rewardslite.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class ListenerPlayerJoin extends AbstractEventListener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && this.plugin.getUpdateChecker().isOutdated()) {
            player.sendMessage(String.format("§6Version §e%s §6of §e%s §6has been released and available over at §bspigotmc.org§6.", this.plugin.getUpdateChecker().getNewestVersion(), this.plugin.getName()));
        }

        this.plugin.getPlayerRepository().getByPlayer(player)
                .thenAcceptAsync(playerData -> playerData.onJoin(player))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
