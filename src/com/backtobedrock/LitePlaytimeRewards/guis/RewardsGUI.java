package com.backtobedrock.LitePlaytimeRewards.guis;

import com.backtobedrock.LitePlaytimeRewards.configs.PlayerData;
import com.backtobedrock.LitePlaytimeRewards.enums.GUIType;
import com.backtobedrock.LitePlaytimeRewards.models.Reward;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RewardsGUI extends PagedGUI {

    private final PlayerData data;

    public RewardsGUI(PlayerData data) {
        super(new CustomHolder(data.getRewards().size(), true, GUIType.REWARDS), (int) Math.ceil((double) data.getRewards().size() / 28));
        this.customHolder.setTitle(this.plugin.getMessages().getRewardsInventoryTitle());
        this.data = data;
        this.initialize();
    }

    @Override
    public void setData() {
        List<Reward> rewards = (List<Reward>) this.data.getRewards().values().stream().sorted(this.plugin.getLPRConfig().getRewardsOrder().getComparator()).collect(Collectors.toList());
        List<Icon> icons = new ArrayList<>();

        rewards.subList((this.currentPage - 1) * 28, this.currentPage * 28 > rewards.size() ? rewards.size() : this.currentPage * 28).forEach(e -> {
            Icon icon = new Icon(this.createGUIItem(e.getcReward().getDisplayName(), e.getRewardsGUIDescription(this.data.getPlayer()), false, e.getcReward().getDisplayItem()), Arrays.asList(), e);
            switch (this.plugin.getLPRConfig().getInventoryLayout()) {
                case FILL:
                    this.customHolder.addIcon(icon);
                    break;
                case CENTERED:
                    icons.add(icon);
                    if (icons.size() == 7) {
                        this.customHolder.addRow(icons);
                        icons.clear();
                    }
                    break;
            }
        });

        if (!icons.isEmpty()) {
            this.customHolder.addRow(icons);
        }
    }
}
