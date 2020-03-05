package com.backtobedrock.LitePlaytimeRewards.helperClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("RedeemedReward")
public class Reward implements ConfigurationSerializable {

    private List<Long> timeTillNextReward;
    private int amountRedeemed;

    public Reward(List<Long> timeTillNextReward, int amountRedeemed) {
        this.timeTillNextReward = timeTillNextReward;
        this.amountRedeemed = amountRedeemed;
    }

    public List<Long> getTimeTillNextReward() {
        return timeTillNextReward;
    }

    public int getAmountRedeemed() {
        return amountRedeemed;
    }

    public void setTimeTillNextReward(List<Long> timeTillNextReward) {
        this.timeTillNextReward = timeTillNextReward;
    }

    public void setAmountRedeemed(int amountRedeemed) {
        this.amountRedeemed = amountRedeemed;
    }

    @Override
    public String toString() {
        return "RedeemedReward{" + "timeTillNextReward=" + timeTillNextReward + ", amountRedeemed=" + amountRedeemed + '}';
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new TreeMap<>();

        map.put("timeTillNextReward", this.timeTillNextReward);
        map.put("amountRedeemed", this.amountRedeemed);

        return map;
    }

    public static Reward deserialize(Map<String, Object> map) {
        List<Long> timeTillNextReward = new ArrayList<>();
        int amountRedeemed = -1;

        if (map.containsKey("timeTillNextReward")) {
            timeTillNextReward = (ArrayList<Long>) map.get("timeTillNextReward");
        }

        if (map.containsKey("amountRedeemed")) {
            amountRedeemed = (int) map.get("amountRedeemed");
        }

        if (timeTillNextReward.isEmpty() || amountRedeemed < 0) {
            return null;
        }

        return new Reward(timeTillNextReward, amountRedeemed);
    }
}
