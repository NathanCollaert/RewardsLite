package com.backtobedrock.LitePlaytimeRewards.guis;

import com.backtobedrock.LitePlaytimeRewards.guis.clickActions.ClickAction;
import com.backtobedrock.LitePlaytimeRewards.models.Reward;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Icon {

    public final ItemStack itemStack;
    public final List<ClickAction> clickActions;
    public final Reward reward;

    public Icon(ItemStack itemStack, List<ClickAction> ca, Reward reward) {
        this.itemStack = itemStack;
        this.clickActions = ca;
        this.reward = reward;
    }

    public List<ClickAction> getClickActions() {
        return this.clickActions;
    }

    public boolean hasReward() {
        return this.reward != null;
    }
}
