package com.backtobedrock.LitePlaytimeRewards.guis;

import com.backtobedrock.LitePlaytimeRewards.enums.GUIType;
import com.backtobedrock.LitePlaytimeRewards.guis.clickActions.ClickAction;
import com.backtobedrock.LitePlaytimeRewards.guis.clickActions.GiveRewardClickAction;
import com.backtobedrock.LitePlaytimeRewards.models.ConfigReward;
import com.backtobedrock.LitePlaytimeRewards.models.GUIReward;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

public class GiveRewardGUI extends PagedGUI {

    private final TreeMap<String, ConfigReward> rewards;
    private final TreeMap<String, GUIReward> GUIRewards = new TreeMap<>();

    public GiveRewardGUI(TreeMap<String, ConfigReward> rewards) {
        super(new CustomHolder(rewards.size(), true, GUIType.GIVE_REWARD), (int) Math.ceil((double) rewards.size() / 28));
        this.customHolder.setTitle(this.plugin.getMessages().getGiveRewardInventoryTitle());
        this.rewards = rewards;
        this.initialize();
    }

    @Override
    protected void setData() {
        new ArrayList<>(this.rewards.entrySet()).subList((this.currentPage - 1) * 28, Math.min(this.currentPage * 28, this.rewards.size())).forEach(e -> {
            GUIReward reward = this.GUIRewards.getOrDefault(e.getKey(), new GUIReward(e.getKey()));
            ItemStack item = this.createGUIItem(e.getValue().getDisplayName(), e.getValue().getGiveRewardGUIDescription(reward), false, e.getValue().getDisplayItem());
            this.customHolder.addIcon(new Icon(item, Arrays.asList(new ClickAction[]{new GiveRewardClickAction(this, reward)}), null));
            this.GUIRewards.putIfAbsent(e.getKey(), reward);
        });
    }
}
