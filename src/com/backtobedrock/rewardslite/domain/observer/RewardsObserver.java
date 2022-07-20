package com.backtobedrock.rewardslite.domain.observer;

import com.backtobedrock.rewardslite.domain.data.PlayerData;
import com.backtobedrock.rewardslite.interfaces.InterfaceRewards;
import com.backtobedrock.rewardslite.utilities.PlayerUtils;

public class RewardsObserver implements IObserver {
    private final InterfaceRewards iface;
    private final PlayerData playerData;

    public RewardsObserver(InterfaceRewards iface, PlayerData playerData) {
        this.iface = iface;
        this.playerData = playerData;
    }

    @Override
    public void update() {
        PlayerUtils.openInventory(this.iface.getSender(), new InterfaceRewards(this.iface.getSender(), this.playerData));
    }
}
