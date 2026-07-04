package com.backtobedrock.rewardslite.domain.afk;

import me.rocketmankianproductions.serveressentials.ServerEssentialsAPI;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class ServerEssentialsAfkHook implements AfkHook {
    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isAfk(OfflinePlayer player) {
        Player online = player.getPlayer();
        return online != null && ServerEssentialsAPI.isAFK(online);
    }
}
