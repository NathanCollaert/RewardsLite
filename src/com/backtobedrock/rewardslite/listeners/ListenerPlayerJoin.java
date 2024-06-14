package com.backtobedrock.rewardslite.listeners;

import com.backtobedrock.rewardslite.domain.enumerations.PluginVersionState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class ListenerPlayerJoin extends AbstractEventListener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.isOp() && this.plugin.getUpdateChecker().getPluginVersionState() != PluginVersionState.LATEST) {
            player.sendMessage(this.plugin.getUpdateChecker().getPluginVersionState().getWarningMessage());
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
