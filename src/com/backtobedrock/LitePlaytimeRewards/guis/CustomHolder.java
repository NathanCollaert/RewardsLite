package com.backtobedrock.LitePlaytimeRewards.guis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class CustomHolder implements InventoryHolder {

    private final Map<Integer, Icon> icons = new HashMap<>();

    private final int size;
    private String title;
    private final int rowAmount;

    public CustomHolder(int size, boolean hasBorder) {
        int amount = hasBorder ? (int) (Math.ceil((double) size / 7) * 9) + 18 : (int) (Math.ceil((double) size / 9) * 9);
        this.size = amount > 54 ? 54 : amount;
        this.rowAmount = this.getSize() / 9;
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

    public void addRow(List<Icon> icons, int row) {
        switch (icons.size()) {
            case 1:
                this.icons.put((row * 9) + 4, icons.get(0));
                break;
            case 2:
                int[] slots2 = {3, 5};
                for (int i = 0; i < icons.size(); i++) {
                    this.icons.put((row * 9) + slots2[i], icons.get(i));
                }
                break;
            case 3:
                int[] slots3 = {2, 4, 6};
                for (int i = 0; i < icons.size(); i++) {
                    this.icons.put((row * 9) + slots3[i], icons.get(i));
                }
                break;
            case 4:
                int[] slots4 = {1, 3, 5, 7};
                for (int i = 0; i < icons.size(); i++) {
                    this.icons.put((row * 9) + slots4[i], icons.get(i));
                }
                break;
        }
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
    
    public void clearContent() {
        this.icons.clear();
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, this.size, this.title);

        this.icons.entrySet().forEach((entry) -> {
            inventory.setItem(entry.getKey(), entry.getValue().itemStack);
        });

        return inventory;
    }
}
