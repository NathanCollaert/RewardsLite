package com.backtobedrock.rewardslite.interfaces;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.CustomHolder;
import com.backtobedrock.rewardslite.domain.Icon;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;

public abstract class AbstractInterface {
    protected final Rewardslite plugin;
    protected CustomHolder customHolder;

    public AbstractInterface(CustomHolder customHolder) {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.customHolder = customHolder;
    }

    protected void initialize() {
        this.createBorder();
    }

    protected void createBorder() {
        for (int i = 0; i < this.customHolder.getSize(); i++) {
            int calc = i % 9;
            if (i < 9 || i >= (this.customHolder.getRowAmount() - 1) * 9 || calc == 0 || calc == 8) {
                this.customHolder.setIcon(i, new Icon(this.plugin.getConfigurations().getInterfacesConfiguration().getFillerDisplay().getItem(), Collections.emptyList()));
            }
        }
    }

    protected void fillInterface(List<Integer> ignore) {
        for (int i = 0; i < this.customHolder.getSize(); i++) {
            if (!ignore.contains(i) && this.customHolder.getIcon(i) == null) {
                this.customHolder.setIcon(i, new Icon(this.plugin.getConfigurations().getInterfacesConfiguration().getFillerDisplay().getItem(), Collections.emptyList()));
            }
        }
    }

    protected void setAccentColor(List<Integer> positions) {
        positions.forEach(e -> this.customHolder.setIcon(e, new Icon(this.plugin.getConfigurations().getInterfacesConfiguration().getAccentDisplay().getItem(), Collections.emptyList())));
    }

    protected abstract void setData();

    public Inventory getInventory() {
        return this.customHolder.getInventory();
    }

    public CustomHolder getCustomHolder() {
        return customHolder;
    }
}
