package com.backtobedrock.rewardslite.utilities;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import com.backtobedrock.rewardslite.interfaces.AbstractInterface;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerUtils {
    public static void openInventory(Player player, AbstractInterface gui) {
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);
        Inventory inventory = gui.getInventory();
        Bukkit.getScheduler().runTask(plugin, () -> {
            player.closeInventory();
            player.openInventory(inventory);
        });
        plugin.addToInterfaces(player, gui);
    }

    public static void closeInventory(Player player) {
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(Rewardslite.class), player::closeInventory);
    }

    public static int getEmptyInventorySlots(Player player) {
        int emptySlots = 0;
        MinecraftVersion minecraftVersion = MinecraftVersion.get();
        ItemStack[] inventory;
        if (minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_9)) {
            inventory = player.getInventory().getStorageContents();
        } else {
            inventory = player.getInventory().getContents();
        }
        for (ItemStack it : inventory) {
            if (it == null) {
                emptySlots++;
            }
        }
        return emptySlots;
    }
}
