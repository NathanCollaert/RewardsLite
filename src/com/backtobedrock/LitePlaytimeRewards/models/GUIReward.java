package com.backtobedrock.LitePlaytimeRewards.models;

public class GUIReward {

    private final String id;
    private int amount;
    private boolean broadcast;

    public GUIReward(String id) {
        this.id = id;
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

    public String getId() {
        return id;
    }
}
