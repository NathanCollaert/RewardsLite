package com.backtobedrock.rewardslite.domain.afk;

import net.lapismc.afkplus.api.AFKPlusPlayerAPI;
import net.lapismc.afkplus.playerdata.AFKPlusPlayer;
import org.bukkit.OfflinePlayer;

public class AfkPlusAfkHook implements AfkHook {
    private final AFKPlusPlayerAPI playerAPI = new AFKPlusPlayerAPI();

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isAfk(OfflinePlayer player) {
        AFKPlusPlayer afkPlusPlayer = this.playerAPI.getPlayer(player);
        return afkPlusPlayer != null && afkPlusPlayer.isAFK();
    }
}
