package com.backtobedrock.rewardslite.listeners;

import com.backtobedrock.rewardslite.domain.data.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

public class ListenerPlayerQuit extends AbstractEventListener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.plugin.getPlayerRepository().getByPlayer(event.getPlayer())
                .thenAcceptAsync(PlayerData::onQuit)
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
