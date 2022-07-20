package com.backtobedrock.rewardslite.domain;

import com.backtobedrock.rewardslite.interfaces.clickActions.AbstractClickAction;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Icon {
    public final ItemStack itemStack;
    public final List<AbstractClickAction> clickActions;

    public Icon(ItemStack itemStack, List<AbstractClickAction> ca) {
        this.itemStack = itemStack;
        this.clickActions = ca;
    }

    public List<AbstractClickAction> getClickActions() {
        return this.clickActions;
    }
}
