package com.backtobedrock.LitePlaytimeRewards.helperClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("Reward")
public class Reward implements ConfigurationSerializable {

    private List<Long> timeTillNextReward;
    private int amountRedeemed;
    private int amountPending;
    private ConfigReward cReward = null;

    public Reward(ConfigReward cReward, List<Long> timeTillNextReward, int amountRedeemed, int amountPending) {
        this(timeTillNextReward.isEmpty() ? cReward.getPlaytimeNeeded() : timeTillNextReward, amountRedeemed < 0 ? 0 : amountRedeemed, amountPending < 0 ? 0 : amountPending);
        this.cReward = cReward;
    }

    //deserialization only
    private Reward(List<Long> timeTillNextReward, int amountRedeemed, int amountPending) {
        this.timeTillNextReward = timeTillNextReward;
        this.amountRedeemed = amountRedeemed;
        this.amountPending = amountPending;
    }

    //new reward only
    public Reward(ConfigReward cReward) {
        this(cReward, cReward.getPlaytimeNeeded(), 0, 0);
    }

    public ConfigReward getcReward() {
        return cReward;
    }

    public List<Long> getTimeTillNextReward() {
        return timeTillNextReward;
    }

    public void setTimeTillNextReward(List<Long> timeTillNextReward) {
        this.timeTillNextReward = timeTillNextReward;
    }

    public int getAmountRedeemed() {
        return amountRedeemed;
    }

    public void setAmountRedeemed(int amountRedeemed) {
        this.amountRedeemed = amountRedeemed;
    }

    public int getAmountPending() {
        return amountPending;
    }

    public void setAmountPending(int amountPending) {
        this.amountPending = amountPending;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new TreeMap<>();

        map.put("timeTillNextReward", this.timeTillNextReward);
        map.put("amountRedeemed", this.amountRedeemed);
        map.put("amountPending", this.amountPending);

        return map;
    }

    public static Reward deserialize(Map<String, Object> map) {
        List<Long> timeTillNextReward = new ArrayList<>();
        int amountRedeemed = -1;
        int amountPending = -1;

        if (map.containsKey("timeTillNextReward") && map.get("timeTillNextReward") != null) {
            ((List<Integer>) map.get("timeTillNextReward")).stream().forEach(e -> {
                timeTillNextReward.add(Long.valueOf(e));
            });
        }

        if (map.containsKey("amountRedeemed") && map.get("amountRedeemed") != null) {
            amountRedeemed = (int) map.get("amountRedeemed");
        }

        if (map.containsKey("amountPending") && map.get("amountPending") != null) {
            amountPending = (int) map.get("amountPending");
        }

        return new Reward(timeTillNextReward, amountRedeemed, amountPending);
    }
}
