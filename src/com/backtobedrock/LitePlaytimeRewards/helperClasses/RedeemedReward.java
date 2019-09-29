package com.backtobedrock.LitePlaytimeRewards.helperClasses;

public class RedeemedReward {

    private long lastPlaytimeCheck;
    private int amountRedeemed;

    public RedeemedReward(long lastPlaytimeCheck, int amountRedeemed) {
        this.lastPlaytimeCheck = lastPlaytimeCheck;
        this.amountRedeemed = amountRedeemed;
    }

    public long getLastPlaytimeCheck() {
        return lastPlaytimeCheck;
    }

    public int getAmountRedeemed() {
        return amountRedeemed;
    }

    public void setLastPlaytimeCheck(long lastPlaytimeCheck) {
        this.lastPlaytimeCheck = lastPlaytimeCheck;
    }

    public void setAmountRedeemed(int amountRedeemed) {
        this.amountRedeemed = amountRedeemed;
    }

    @Override
    public String toString() {
        return "RedeemedReward{" + "lastPlaytimeCheck=" + lastPlaytimeCheck + ", amountRedeemed=" + amountRedeemed + '}';
    }
}
