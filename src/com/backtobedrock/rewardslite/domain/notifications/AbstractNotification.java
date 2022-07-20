package com.backtobedrock.rewardslite.domain.notifications;

import org.bukkit.entity.Player;

import java.util.List;

public abstract class AbstractNotification {
    protected final boolean enabled;

    protected AbstractNotification(boolean enabled) {
        this.enabled = enabled;
    }

    public abstract void notify(Player player);

    public void notify(List<Player> players) {
        players.forEach(this::notify);
    }

    public boolean isEnabled() {
        return enabled;
    }
}
