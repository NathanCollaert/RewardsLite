package com.backtobedrock.LitePlaytimeRewards.helperClasses;

import java.util.Map;
import java.util.TreeMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("RedeemedReward")
public class RedeemedReward implements ConfigurationSerializable {

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

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new TreeMap<>();

        map.put("lastPlaytimeCheck", this.lastPlaytimeCheck);
        map.put("amountRedeemed", this.amountRedeemed);

        return map;
    }

    public static RedeemedReward deserialize(Map<String, Object> map) {
        long lastPlaytimeCheck = -1;
        int amountRedeemed = -1;

        if (map.containsKey("lastPlaytimeCheck")) {
            lastPlaytimeCheck = Long.valueOf(String.valueOf(map.get("lastPlaytimeCheck")));
        }

        if (map.containsKey("amountRedeemed")) {
            amountRedeemed = (int) map.get("amountRedeemed");
        }

        if (lastPlaytimeCheck < 0 || amountRedeemed < 0) {
            return null;
        }
        return new RedeemedReward(lastPlaytimeCheck, amountRedeemed);
    }
}
