package com.backtobedrock.rewardslite.domain.afk;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

/**
 * ServerEssentials publishes no Maven/JitPack artifact, so its API is called via reflection instead of a compile-time dependency.
 */
public class ServerEssentialsAfkHook implements AfkHook {
    private final Method isAfkMethod;

    public ServerEssentialsAfkHook() throws ReflectiveOperationException {
        Class<?> apiClass = Class.forName("me.rocketmankianproductions.serveressentials.api.ServerEssentialsAPI");
        this.isAfkMethod = apiClass.getMethod("isAFK", Player.class);
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isAfk(OfflinePlayer player) {
        Player online = player.getPlayer();
        if (online == null) {
            return false;
        }

        try {
            return (boolean) this.isAfkMethod.invoke(null, online);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
