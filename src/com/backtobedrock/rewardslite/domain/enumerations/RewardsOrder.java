package com.backtobedrock.rewardslite.domain.enumerations;

import com.backtobedrock.rewardslite.domain.RewardData;
import org.bukkit.OfflinePlayer;

import java.util.Comparator;

public enum RewardsOrder {
    FILENAME(Comparator.comparing(RewardData::getFileName)),
    FILENAME_REVERSED(Comparator.comparing(RewardData::getFileName).reversed()),
    TITLE(Comparator.comparing(r -> r.getDisplay().getName())),
    TITLE_REVERSED(Comparator.comparing((RewardData r) -> r.getDisplay().getName()).reversed()),
    STATUS(null),
    STATUS_REVERSED(null),
    TIME_REQUIRED(Comparator.comparingLong(RewardData::getRequiredTime)),
    TIME_REQUIRED_REVERSED(Comparator.comparingLong(RewardData::getRequiredTime).reversed()),
    TIME_LEFT(Comparator.comparingLong(RewardData::getTimeLeft)),
    TIME_LEFT_REVERSED(Comparator.comparingLong(RewardData::getTimeLeft).reversed()),
    PENDING(Comparator.comparingInt(RewardData::getPending)),
    PENDING_REVERSED(Comparator.comparingInt(RewardData::getPending).reversed()),
    REDEEMED(Comparator.comparingInt(RewardData::getRedeemed)),
    REDEEMED_REVERSED(Comparator.comparingInt(RewardData::getRedeemed).reversed());

    private final Comparator<RewardData> comparator;

    RewardsOrder(Comparator<RewardData> comparator) {
        this.comparator = comparator;
    }

    public Comparator<RewardData> getComparator(OfflinePlayer player) {
        return this == STATUS ? Comparator.comparing(r -> RewardStatus.getRewardStatus(r, player)) : this == STATUS_REVERSED ? Comparator.comparing((RewardData r) -> RewardStatus.getRewardStatus(r, player)).reversed() : this.comparator;
    }
}
