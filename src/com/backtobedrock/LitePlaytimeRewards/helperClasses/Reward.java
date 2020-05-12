package com.backtobedrock.LitePlaytimeRewards.helperClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("Reward")
public class Reward implements ConfigurationSerializable {

    private List<Integer> timeTillNextReward;
    private int amountRedeemed;
    private int amountPending;
    private boolean eligible;
    private ConfigReward cReward = null;

    public Reward(ConfigReward cReward, List<Integer> timeTillNextReward, int amountRedeemed, int amountPending, boolean eligible) {
        this(timeTillNextReward.isEmpty() ? new ArrayList(cReward.getPlaytimeNeeded()) : timeTillNextReward, amountRedeemed < 0 ? 0 : amountRedeemed, amountPending < 0 ? 0 : amountPending, cReward.isLoop() ? true : eligible);
        this.cReward = cReward;
    }

    //deserialization only
    private Reward(List<Integer> timeTillNextReward, int amountRedeemed, int amountPending, boolean eligible) {
        this.timeTillNextReward = timeTillNextReward;
        this.amountRedeemed = amountRedeemed;
        this.amountPending = amountPending;
        this.eligible = eligible;
    }

    //new reward only
    public Reward(ConfigReward cReward) {
        this(cReward, new ArrayList(cReward.getPlaytimeNeeded()), 0, 0, true);
    }

    public ConfigReward getcReward() {
        return cReward;
    }

    public List<Integer> getTimeTillNextReward() {
        return timeTillNextReward;
    }

    public void removeFirstTimeTillNextReward() {
        this.timeTillNextReward.remove(0);
        if (this.timeTillNextReward.isEmpty()) {
            if (!this.cReward.isLoop()) {
                this.eligible = false;
            }
            this.timeTillNextReward = new ArrayList(this.cReward.getPlaytimeNeeded());
        }
    }

    public void resetTimeTillNextReward() {
        this.timeTillNextReward = new ArrayList(this.cReward.getPlaytimeNeeded());
    }

    public void setFirstTimeTillNextReward(int time) {
        this.timeTillNextReward.set(0, time);
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

    public boolean isEligible() {
        return this.eligible;
    }

    @Override
    public String toString() {
        return "Reward{" + "timeTillNextReward=" + timeTillNextReward + ", amountRedeemed=" + amountRedeemed + ", amountPending=" + amountPending + ", eligible=" + eligible + ", cReward=" + cReward + '}';
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new TreeMap<>();

        map.put("timeTillNextReward", this.timeTillNextReward);
        map.put("amountRedeemed", this.amountRedeemed);
        map.put("amountPending", this.amountPending);
        map.put("eligible", this.eligible);

        return map;
    }

    public static Reward deserialize(Map<String, Object> map) {
        List<Integer> timeTillNextReward = new ArrayList<>();
        int amountRedeemed = -1;
        int amountPending = -1;
        boolean eligible = true;

        if (map.containsKey("timeTillNextReward") && map.get("timeTillNextReward") != null) {
            timeTillNextReward = (List<Integer>) map.get("timeTillNextReward");
        }

        if (map.containsKey("amountRedeemed") && map.get("amountRedeemed") != null) {
            amountRedeemed = (int) map.get("amountRedeemed");
        }

        if (map.containsKey("amountPending") && map.get("amountPending") != null) {
            amountPending = (int) map.get("amountPending");
        }

        if (map.containsKey("eligible") && map.get("eligible") != null) {
            if (!timeTillNextReward.isEmpty() && timeTillNextReward.get(0) == -1) {
                eligible = false;
                timeTillNextReward = new ArrayList<>();
            } else {
                eligible = (boolean) map.get("eligible");
            }
        }

        return new Reward(timeTillNextReward, amountRedeemed, amountPending, eligible);
    }
}
