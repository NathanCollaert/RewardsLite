package com.backtobedrock.rewardslite.domain.afk;

import com.earth2me.essentials.User;
import net.ess3.api.IEssentials;
import org.bukkit.OfflinePlayer;

public class EssentialsAfkHook implements AfkHook {
    private final IEssentials essentials;

    public EssentialsAfkHook(IEssentials essentials) {
        this.essentials = essentials;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isAfk(OfflinePlayer player) {
        User user = this.essentials.getUser(player.getUniqueId());
        return user != null && user.isAfk();
    }
}
