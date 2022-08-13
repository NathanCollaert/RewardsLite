package com.backtobedrock.rewardslite.domain.enumerations;

import com.backtobedrock.rewardslite.domain.RewardData;
import org.bukkit.OfflinePlayer;

public enum RewardStatus {
    ELIGIBLE,
    FINISHED,
    INELIGIBLE;

    public static RewardStatus getRewardStatus(RewardData rewardData, OfflinePlayer player) {
        if (!rewardData.isActive()) {
            return FINISHED;
        } else if (player.getPlayer() == null || rewardData.hasPermission(player.getPlayer())) {
            return ELIGIBLE;
        }
        return INELIGIBLE;
    }
}
