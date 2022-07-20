package com.backtobedrock.rewardslite.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class ListenerPlayerJoin extends AbstractEventListener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.plugin.getPlayerRepository().getByPlayer(event.getPlayer())
                .thenAcceptAsync(playerData -> playerData.onJoin(event.getPlayer()))
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
