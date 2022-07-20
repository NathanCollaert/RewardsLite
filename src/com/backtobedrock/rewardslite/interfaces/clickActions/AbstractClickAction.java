package com.backtobedrock.rewardslite.interfaces.clickActions;

import com.backtobedrock.rewardslite.Rewardslite;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractClickAction {

    protected final Rewardslite plugin;

    public AbstractClickAction() {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
    }

    public abstract void execute(Player player);
}
