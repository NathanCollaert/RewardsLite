package com.backtobedrock.rewardslite.domain;

import com.backtobedrock.rewardslite.Rewardslite;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomHolder implements InventoryHolder {
    private final Rewardslite plugin;

    private final String title;
    private final int size;
    private final int rowAmount;
    private final Map<Integer, Icon> icons = new HashMap<>();
    private int currentRow = 1;
    private Inventory inventory = null;

    public CustomHolder(int size, String title) {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.title = title;
        this.size = Math.min((int) (Math.ceil((double) Math.max(1, size) / 7) * 9) + 18, 54);
        this.rowAmount = this.getSize() / 9;
    }

    public void setIcon(int position, Icon icon) {
        this.icons.put(position, icon);
    }

    public void setIcon(int position, Icon icon, boolean update) {
        this.setIcon(position, icon);
        if (update) {
            this.updateIcon(position);
        }
    }

    public int addIcon(Icon icon) {
        int position = -1;
        for (int i = 0; i < this.size; i++) {
            if (!this.icons.containsKey(i)) {
                this.icons.put(i, icon);
                position = i;
                break;
            }
        }
        return position;
    }

    public void addIcons(List<Icon> icons) {
        int amountOnRow = 1;
        for (Icon i : icons) {
            this.icons.put((this.currentRow * 9) + amountOnRow, i);
            amountOnRow++;
            if (amountOnRow == 8) {
                this.currentRow++;
                amountOnRow = 1;
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
                int[] slots5 = {1, 2, 4, 6, 7};
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
        this.currentRow++;
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

    public void reset() {
        this.icons.clear();
        this.currentRow = 1;
    }

    public void setCurrentRow(int currentRow) {
        this.currentRow = currentRow;
    }

    @Override
    public Inventory getInventory() {
        if (this.inventory == null) {
            this.inventory = Bukkit.createInventory(this, this.size, this.title);
            this.icons.forEach((key, value) -> this.inventory.setItem(key, value.itemStack));
        }
        return this.inventory;
    }

    public void updateIcon(int position) {
        if (this.inventory != null && this.icons.containsKey(position)) {
            this.getInventory().setItem(position, this.icons.get(position).itemStack);
        }
    }

    public void updateInvent() {
        if (this.inventory != null) {
            for (int i = 0; i < this.size; i++) {
                Icon icon = this.icons.get(i);
                this.getInventory().setItem(i, icon == null ? new ItemStack(Material.AIR) : icon.itemStack);
            }
        }
    }
}
