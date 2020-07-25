package com.backtobedrock.LitePlaytimeRewards.guis.clickActions;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AdvancedClickAction implements ActionHandler{

    protected LitePlaytimeRewards plugin;

    protected AdvancedClickAction() {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
    }

    public abstract void execute(Player player, ClickType type);
}
