package com.backtobedrock.rewardslite.utilities;

import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class ItemUtils {
    public static ItemStack createItem(Material material, String displayName, List<String> lore, int amount, boolean glowing) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta im = item.getItemMeta();
        if (im != null) {
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            im.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            im.setDisplayName(displayName);
            im.setLore(lore);
            if (glowing) {
                im.addEnchant(Enchantment.INFINITY, 0, true);
                im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(im);
        }
        return item;
    }

    public static ItemStack createPlayerSkull(String displayName, List<String> lore, OfflinePlayer player) {
        MinecraftVersion minecraftVersion = MinecraftVersion.get();
        if (minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_13)) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) item.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(player);
                sm.setDisplayName(displayName);
                sm.setLore(lore);
                item.setItemMeta(sm);
            }
            return item;
        }
        return null;
    }
}
