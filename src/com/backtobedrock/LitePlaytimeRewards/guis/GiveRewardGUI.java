package com.backtobedrock.LitePlaytimeRewards.guis;

import com.backtobedrock.LitePlaytimeRewards.guis.clickActions.ActionHandler;
import com.backtobedrock.LitePlaytimeRewards.guis.clickActions.GiveRewardClickAction;
import com.backtobedrock.LitePlaytimeRewards.models.ConfigReward;
import com.backtobedrock.LitePlaytimeRewards.models.GUIReward;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.bukkit.inventory.ItemStack;

public class GiveRewardGUI extends PagedGUI {

    private final TreeMap<String, ConfigReward> rewards;
    private final TreeMap<String, GUIReward> GUIRewards = new TreeMap<>();

    public GiveRewardGUI(TreeMap<String, ConfigReward> rewards) {
        super(new CustomHolder(rewards.size(), true), (int) Math.ceil((double) rewards.size() / 28));
        this.customHolder.setTitle(this.plugin.getMessages().getGiveRewardInventoryTitle());
        this.rewards = rewards;
        this.initialize();
    }

    @Override
    protected void setData() {
        this.rewards.entrySet().stream().collect(Collectors.toList()).subList((this.currentPage - 1) * 28, this.currentPage * 28 > this.rewards.size() ? this.rewards.size() : this.currentPage * 28).forEach(e -> {
            GUIReward reward = this.GUIRewards.getOrDefault(e.getKey(), new GUIReward(e.getKey()));
            ItemStack item = this.createGUIItem(e.getValue().getDisplayName(), e.getValue().getGiveRewardGUIDescription(reward), false, e.getValue().getDisplayItem());
            this.customHolder.addIcon(new Icon(item, Arrays.asList(new ActionHandler[]{new GiveRewardClickAction(this, reward)})));
            this.GUIRewards.putIfAbsent(e.getKey(), reward);
        });
    }
}
