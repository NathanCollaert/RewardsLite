package com.backtobedrock.rewardslite.interfaces.clickActions;

import com.backtobedrock.rewardslite.domain.RewardData;
import org.bukkit.entity.Player;

public class ClickActionRedeemReward extends AbstractClickAction {
    private final RewardData rewardData;

    public ClickActionRedeemReward(RewardData rewardData) {
        this.rewardData = rewardData;
    }

    @Override
    public void execute(Player player) {
        this.rewardData.claimPending(player);
    }
}
