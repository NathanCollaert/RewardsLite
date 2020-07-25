package com.backtobedrock.LitePlaytimeRewards.guis;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.configs.Config;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class GUI {

    protected final LitePlaytimeRewards plugin;
    protected final Config config;
    protected final CustomHolder customHolder;

    public GUI(CustomHolder customHolder) {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        this.config = this.plugin.getLPRConfig();
        this.customHolder = customHolder;
    }

    protected void initialize() {
        ItemStack borderItem = new ItemStack(this.config.getBorderMaterial());
        ItemMeta im = borderItem.getItemMeta();
        if (im != null) {
            im.setDisplayName(" ");
            borderItem.setItemMeta(im);
        }

        for (int i = 0; i < this.customHolder.getSize(); i++) {
            int calc = i % 9;
            if (i < 9 || i >= (this.customHolder.getRowAmount() - 1) * 9 || calc == 0 || calc == 8) {
                this.customHolder.setIcon(i, new Icon(borderItem, Arrays.asList()));
            }
        }
    }

    protected ItemStack createGUIItem(String displayName, List<String> lore, boolean glowing, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta im = item.getItemMeta();
        if (im != null) {
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

    public Inventory getInventory() {
        return this.customHolder.getInventory();
    }
}
