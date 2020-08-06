package com.backtobedrock.LitePlaytimeRewards.guis.clickActions;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class ClickAction {

    protected LitePlaytimeRewards plugin;

    protected ClickAction() {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
    }

    public abstract void execute(Player player, ClickType type);

    public boolean hasEnoughInventory(Player player, ItemStack item) {
        //check if enough free invent space
        int emptySlots = 0;
        for (ItemStack it : player.getInventory().getStorageContents()) {
            if (emptySlots > 0) {
                break;
            }
            if (it == null || (it.isSimilar(item) && it.getAmount() < it.getMaxStackSize())) {
                emptySlots++;
            }
        }
        return emptySlots > 0;
    }
}
