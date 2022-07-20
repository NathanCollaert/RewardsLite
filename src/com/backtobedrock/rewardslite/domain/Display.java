package com.backtobedrock.rewardslite.domain;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.utilities.ConfigUtils;
import com.backtobedrock.rewardslite.utilities.ItemUtils;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class Display {
    private final Material material;
    private final boolean glow;
    private final String name;
    private final List<String> lore;
    private final int amount;

    public Display(Material material, boolean glow, String name, List<String> lore, int amount) {
        this.material = material;
        this.glow = glow;
        this.name = name;
        this.lore = lore;
        this.amount = amount;
    }

    public static Display deserialize(String id, ConfigurationSection section) {
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);

        Material cMaterial = ConfigUtils.getMaterial(id + ".material", section.getString("material"));
        boolean cGlow = section.getBoolean("glow", false);
        String cName = section.getString("name");
        List<String> cLore = section.getStringList("lore");
        int cAmount = ConfigUtils.checkMinMax(id + ".amount", section.getInt("amount", 1), 1, Integer.MAX_VALUE);

        if (cName == null) {
            plugin.getLogger().log(Level.SEVERE, id + ".name: %s is not a valid name.");
            return null;
        }

        if (cAmount == -10 || cMaterial == null) {
            return null;
        }

        return new Display(cMaterial, cGlow, cName, cLore, cAmount);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> object = new HashMap<>();
        object.put("material", getMaterial().toString());
        object.put("glow", isGlow() ? isGlow() : null);
        object.put("name", getName());
        object.put("lore", getLore());
        object.put("amount", getAmount() > 1 ? getAmount() : null);
        return object;
    }

    public ItemStack getItem() {
        return this.getItem(new HashMap<>());
    }

    public ItemStack getItem(Map<String, String> placeholders) {
        return MessageUtils.replaceItemNameAndLorePlaceholders(ItemUtils.createItem(this.material, this.getName(), this.getLore(), this.amount, this.glow), placeholders);
    }

    public String getName() {
        return this.name;
    }

    public List<String> getLore() {
        return this.lore;
    }

    public Material getMaterial() {
        return material;
    }

    public boolean isGlow() {
        return glow;
    }

    public int getAmount() {
        return amount;
    }
}
