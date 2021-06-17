package com.backtobedrock.LitePlaytimeRewards.enums;

import com.backtobedrock.LitePlaytimeRewards.models.Reward;

import java.util.Comparator;

public enum RewardsOrder {
    ID(Comparator.comparing(Reward::getId)),
    ID_REVERSED(Comparator.comparing(Reward::getId).reversed()),
    NAME(Comparator.comparing(Reward::getName)),
    NAME_REVERSED(Comparator.comparing(Reward::getName).reversed()),
    TIME(Comparator.comparingInt(Reward::getFirstTimeTillNextReward)),
    TIME_REVERSED(Comparator.comparingInt(Reward::getFirstTimeTillNextReward).reversed()),
    PENDING(Comparator.comparingInt(Reward::getAmountPending)),
    PENDING_REVERSED(Comparator.comparingInt(Reward::getAmountPending).reversed()),
    REDEEMED(Comparator.comparingInt(Reward::getAmountRedeemed)),
    REDEEMED_REVERSED(Comparator.comparingInt(Reward::getAmountRedeemed).reversed());

    private final Comparator comparator;

    RewardsOrder(Comparator comparator) {
        this.comparator = comparator;
    }

    public Comparator getComparator() {
        return this.comparator;
    }
}
