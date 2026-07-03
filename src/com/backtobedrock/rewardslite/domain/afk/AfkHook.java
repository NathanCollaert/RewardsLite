package com.backtobedrock.rewardslite.domain.afk;

import org.bukkit.OfflinePlayer;

public interface AfkHook {
    /**
     * @return Whether this plugin's own AFK detection is turned on. Checked live since some plugins allow toggling this without a restart.
     */
    boolean isActive();

    boolean isAfk(OfflinePlayer player);
}
