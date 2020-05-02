package com.backtobedrock.LitePlaytimeRewards.helperClasses;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class RewardsGUICustomHolder implements InventoryHolder {

    private final int size;
    private final String title;

    public RewardsGUICustomHolder(int size, String title) {
        this.size = size;
        this.title = title;
    }

    @Override
    public Inventory getInventory() {
        return Bukkit.createInventory(this, this.size, this.title);
    }

    public String getTitle() {
        return title;
    }
}
