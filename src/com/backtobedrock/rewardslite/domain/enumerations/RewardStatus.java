package com.backtobedrock.rewardslite.domain.enumerations;

import com.backtobedrock.rewardslite.domain.RewardData;
import org.bukkit.entity.Player;

public enum RewardStatus {
    ELIGIBLE,
    FINISHED,
    INELIGIBLE;

    public static RewardStatus getRewardStatus(RewardData rewardData, Player player) {
        if (!rewardData.isActive()) {
            return FINISHED;
        } else if (rewardData.hasPermission(player)) {
            return ELIGIBLE;
        }
        return INELIGIBLE;
    }
}
