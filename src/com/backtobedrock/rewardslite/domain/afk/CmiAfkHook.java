package com.backtobedrock.rewardslite.domain.afk;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import org.bukkit.OfflinePlayer;

public class CmiAfkHook implements AfkHook {
    @Override
    public boolean isActive() {
        return CMI.getInstance() != null && CMI.getInstance().getAfkManager() != null && CMI.getInstance().getAfkManager().isAfkEnabled();
    }

    @Override
    public boolean isAfk(OfflinePlayer player) {
        CMIUser user = CMIUser.getUser(player);
        return user != null && user.isAfk();
    }
}
