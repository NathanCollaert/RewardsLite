package com.backtobedrock.LitePlaytimeRewards.helperClasses;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

@SerializableAs("Reward")
public class Reward extends ConfigReward implements ConfigurationSerializable {

    private String id;
    private List<Long> timeTillNextReward;
    private int amountRedeemed;
    private int amountPending;

    public Reward(ConfigReward config, String id, List<Long> timeTillNextReward, int amountRedeemed, int amountPending) {
        super(config);
        this.id = id;
        this.timeTillNextReward = timeTillNextReward;
        this.amountRedeemed = amountRedeemed;
        this.amountPending = amountPending;
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

    public int getAmountPending() {
        return amountPending;
    }

    public void setAmountPending(int amountPending) {
        this.amountPending = amountPending;
    }

    @Override
    public String toString() {
        return "Reward{" + "timeTillNextReward=" + timeTillNextReward + ", amountRedeemed=" + amountRedeemed + ", amountPending=" + amountPending + '}';
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new TreeMap<>();

        map.put("id", this.id);
        map.put("timeTillNextReward", this.timeTillNextReward);
        map.put("amountRedeemed", this.amountRedeemed);
        map.put("amountPending", this.amountPending);

        return map;
    }

    public static Reward deserialize(Map<String, Object> map) {
        String id = "";
        List<Long> timeTillNextReward = new ArrayList<>();
        int amountRedeemed = -1;
        int amountPending = -1;

        if (map.containsKey("id")) {
            id = (String) map.get("id");
        }

        if (map.containsKey("timeTillNextReward")) {
            List<Long> longs = new ArrayList<>();
            ((List<Integer>) map.get("timeTillNextReward")).stream().forEach(e -> {
                longs.add(Long.valueOf(e));
            });
            timeTillNextReward = longs;
        }

        if (map.containsKey("amountRedeemed")) {
            amountRedeemed = (int) map.get("amountRedeemed");
        }

        if (map.containsKey("amountPending")) {
            amountPending = (int) map.get("amountPending");
        }

        if (id.equals("") || amountRedeemed < 0 || amountPending < 0) {
            return null;
        }

        ConfigReward creward = LitePlaytimeRewards.getInstance().getLPRConfig().getRewards().get(id);
        if (creward != null) {
            return new Reward(creward, id, timeTillNextReward, amountRedeemed, amountPending);
        }
        return null;
    }
}
