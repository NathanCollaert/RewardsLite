package com.backtobedrock.LitePlaytimeRewards.guis;

import com.backtobedrock.LitePlaytimeRewards.guis.clickActions.ActionHandler;
import java.util.List;
import org.bukkit.inventory.ItemStack;

public class Icon {

    public final ItemStack itemStack;
    public final List<ActionHandler> clickActions;

    public Icon(ItemStack itemStack, List<ActionHandler> ca) {
        this.itemStack = itemStack;
        this.clickActions = ca;
    }

    public List<ActionHandler> getClickActions() {
        return this.clickActions;
    }
}
