package com.backtobedrock.LitePlaytimeRewards.guis;

import com.backtobedrock.LitePlaytimeRewards.configs.PlayerData;
import com.backtobedrock.LitePlaytimeRewards.models.Reward;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RewardsGUI extends PagedGUI {
    
    private final PlayerData data;
    
    public RewardsGUI(PlayerData data) {
        super(new CustomHolder(data.getRewards().size(), true), (int) Math.ceil((double) data.getRewards().size() / 28));
        this.customHolder.setTitle(this.plugin.getMessages().getRewardsInventoryTitle());
        this.data = data;
        this.initialize();
    }
    
    @Override
    public void setData() {
        List<Reward> rewards = this.data.getRewards().values().stream().collect(Collectors.toList());
        rewards.subList((this.currentPage - 1) * 28, this.currentPage * 28 > rewards.size() ? rewards.size() : this.currentPage * 28).forEach(e -> {
            this.customHolder.addIcon(new Icon(this.createGUIItem(e.getcReward().getDisplayName(), e.getRewardsGUIDescription(this.data.getPlayer()), false, e.getcReward().getDisplayItem()), Arrays.asList()));
        });
    }
}
