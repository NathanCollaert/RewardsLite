package com.backtobedrock.rewardslite.utilities;

import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.List;

public class ItemUtils {
    public static ItemStack createItem(Material material, String displayName, List<String> lore, int amount, boolean glowing) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta im = item.getItemMeta();
        if (im != null) {
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            im.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            im.setDisplayName(displayName);
            im.setLore(lore);
            if (glowing) {
                im.addEnchant(Enchantment.ARROW_INFINITE, 0, true);
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

    public static ItemStack createPotion(String displayName, List<String> lore) {
        ItemStack item = new ItemStack(Material.POTION);
        PotionMeta sm = (PotionMeta) item.getItemMeta();
        if (sm != null) {
            MinecraftVersion minecraftVersion = MinecraftVersion.get();
            if (minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_9)) {
                sm.setBasePotionData(new PotionData(PotionType.SPEED));
            }
            sm.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            sm.setDisplayName(displayName);
            sm.setLore(lore);
            item.setItemMeta(sm);
        }
        return item;
    }
}
