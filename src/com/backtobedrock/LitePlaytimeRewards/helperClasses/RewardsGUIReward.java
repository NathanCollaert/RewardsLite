package com.backtobedrock.LitePlaytimeRewards.helperClasses;

public class RewardsGUIReward {

    private int amount;
    private boolean broadcast;

    public RewardsGUIReward() {
        this.amount = 1;
        this.broadcast = false;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public void increaseAmount() {
        this.amount++;
    }

    public void decreaseAmount() {
        if (this.amount > 1) {
            this.amount--;
        }
    }

    public void toggleBroadcast() {
        this.broadcast = !broadcast;
    }
}
