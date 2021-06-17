package com.backtobedrock.LitePlaytimeRewards.guis;

import com.backtobedrock.LitePlaytimeRewards.enums.GUIType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomHolder implements InventoryHolder {

    private final Map<Integer, Icon> icons = new HashMap<>();

    private final int size;
    private final int rowAmount;
    private final GUIType type;
    private String title;
    private int currentRow = 1;
    private Inventory inventory = null;

    public CustomHolder(int size, boolean hasBorder, GUIType type) {
        int amount = hasBorder ? (int) (Math.ceil((double) size / 7) * 9) + 18 : (int) (Math.ceil((double) size / 9) * 9);
        this.size = Math.min(amount, 54);
        this.rowAmount = this.getSize() / 9;
        this.type = type;
    }

    public void setIcon(int position, Icon icon) {
        this.icons.put(position, icon);
    }

    public void addIcon(Icon icon) {
        for (int i = 0; i < this.size; i++) {
            if (!this.icons.containsKey(i)) {
                this.icons.put(i, icon);
                break;
            }
        }
    }

    public void addRow(List<Icon> icons) {
        switch (icons.size()) {
            case 1:
                this.icons.put((this.currentRow * 9) + 4, icons.get(0));
                break;
            case 2:
                int[] slots2 = {3, 5};
                for (int i = 0; i < icons.size(); i++) {
                    this.icons.put((this.currentRow * 9) + slots2[i], icons.get(i));
                }
                break;
            case 3:
                int[] slots3 = {2, 4, 6};
                for (int i = 0; i < icons.size(); i++) {
                    this.icons.put((this.currentRow * 9) + slots3[i], icons.get(i));
                }
                break;
            case 4:
                int[] slots4 = {1, 3, 5, 7};
                for (int i = 0; i < icons.size(); i++) {
                    this.icons.put((this.currentRow * 9) + slots4[i], icons.get(i));
                }
                break;
            case 5:
                int[] slots5 = {2, 3, 4, 5, 6};
                for (int i = 0; i < icons.size(); i++) {
                    this.icons.put((this.currentRow * 9) + slots5[i], icons.get(i));
                }
                break;
            case 6:
                int[] slots6 = {1, 2, 3, 5, 6, 7};
                for (int i = 0; i < icons.size(); i++) {
                    this.icons.put((this.currentRow * 9) + slots6[i], icons.get(i));
                }
                break;
            case 7:
                int[] slots7 = {1, 2, 3, 4, 5, 6, 7};
                for (int i = 0; i < icons.size(); i++) {
                    this.icons.put((this.currentRow * 9) + slots7[i], icons.get(i));
                }
                break;
        }
        this.currentRow += 1;
    }

    public Icon getIcon(int position) {
        return this.icons.get(position);
    }

    public int getSize() {
        return size;
    }

    public int getRowAmount() {
        return rowAmount;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public GUIType getType() {
        return type;
    }

    public void clearContent() {
        this.icons.clear();
    }

    @Override
    public Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = Bukkit.createInventory(this, this.size, this.title);

            this.icons.forEach((key, value) -> this.inventory.setItem(key, value.itemStack));
        }
        return this.inventory;
    }

    public void updateRewards(Player player) {
        this.icons.entrySet().stream().filter(e -> e.getValue().hasReward()).forEach((entry) -> {
            this.updateInventoryItem(this.inventory.getItem(entry.getKey()), entry.getValue(), player);
        });
    }

    private void updateInventoryItem(ItemStack item, Icon icon, Player player) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setLore(icon.reward.getRewardsGUIDescription(player));
            item.setItemMeta(meta);
        }
    }
}
