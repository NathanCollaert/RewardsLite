package com.backtobedrock.rewardslite.listeners;

import com.backtobedrock.rewardslite.Rewardslite;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractEventListener implements Listener {
    protected final Rewardslite plugin;

    public AbstractEventListener() {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
    }

    public abstract boolean isEnabled();
}
