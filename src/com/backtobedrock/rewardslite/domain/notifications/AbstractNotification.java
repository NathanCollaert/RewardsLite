package com.backtobedrock.rewardslite.domain.notifications;

import org.bukkit.entity.Player;

import java.util.List;

public abstract class AbstractNotification {
    protected final boolean enabled;

    protected AbstractNotification(boolean enabled) {
        this.enabled = enabled;
    }

    public abstract void notify(Player player, String playerName);

    public void notify(List<Player> players, String playerName) {
        players.forEach(p -> this.notify(p, playerName));
    }

    public boolean isEnabled() {
        return enabled;
    }
}
