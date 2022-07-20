package com.backtobedrock.rewardslite.domain.observer;

import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.interfaces.InterfaceRewards;

public class RewardObserver implements IObserver {
    private final InterfaceRewards iface;
    private final RewardData rewardData;

    public RewardObserver(InterfaceRewards iface, RewardData rewardData) {
        this.iface = iface;
        this.rewardData = rewardData;
    }

    @Override
    public void update() {
        this.iface.updateReward(true, this.rewardData);
    }
}
