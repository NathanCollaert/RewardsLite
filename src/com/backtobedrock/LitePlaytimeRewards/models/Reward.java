package com.backtobedrock.LitePlaytimeRewards.models;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@SerializableAs("Reward")
public class Reward implements ConfigurationSerializable {

    private final LitePlaytimeRewards plugin;

    private List<Integer> timeTillNextReward;
    private int amountRedeemed;
    private int amountPending;
    private boolean eligible;
    private boolean claimedOldPlaytime;
    private ConfigReward cReward;

    public Reward(ConfigReward cReward, List<Integer> timeTillNextReward, int amountRedeemed, int amountPending, boolean eligible, boolean claimedOldPlaytime) {
        this(timeTillNextReward.isEmpty() ? new ArrayList(cReward.getPlaytimeNeeded()) : timeTillNextReward, amountRedeemed < 0 ? 0 : amountRedeemed, amountPending < 0 ? 0 : amountPending, cReward.isLoop() ? true : eligible, claimedOldPlaytime);
        this.cReward = cReward;
    }

    //deserialization only
    private Reward(List<Integer> timeTillNextReward, int amountRedeemed, int amountPending, boolean eligible, boolean claimedOldPlaytime) {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        this.timeTillNextReward = timeTillNextReward;
        this.amountRedeemed = amountRedeemed;
        this.amountPending = amountPending;
        this.eligible = eligible;
        this.claimedOldPlaytime = claimedOldPlaytime;
    }

    //new reward only
    public Reward(ConfigReward cReward) {
        this(cReward, new ArrayList(cReward.getPlaytimeNeeded()), 0, 0, true, false);
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

    public boolean isClaimedOldPlaytime() {
        return claimedOldPlaytime;
    }

    public void setClaimedOldPlaytime(boolean claimedOldPlaytime) {
        this.claimedOldPlaytime = claimedOldPlaytime;
    }

    public List<String> getRewardsGUIDescription(OfflinePlayer player) {
        List<String> description = (this.cReward.getDisplayDescription().stream().collect(Collectors.toList()));

        if (description.size() > 0) {
            description.add(0, "§f----");
        }

        description.addAll(0, this.plugin.getMessages().getRewardInfo(this.getAmountRedeemed(), this.getAmountPending()));
        description.add("§f----");
        int nextReward = this.getTimeTillNextReward().get(0);
        description.addAll(player.isOnline() && (((Player) player).hasPermission("liteplaytimerewards.reward." + this.cReward.getId()) || !this.cReward.isUsePermission())
                ? this.isEligible()
                ? this.plugin.getMessages().getNextReward(nextReward)
                : this.plugin.getMessages().getNextRewardNever()
                : this.plugin.getMessages().getNextRewardNoPermission());

        return description;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new TreeMap<>();

        map.put("timeTillNextReward", this.timeTillNextReward);
        map.put("amountRedeemed", this.amountRedeemed);
        map.put("amountPending", this.amountPending);
        map.put("eligible", this.eligible);
        map.put("claimedOldPlaytime", this.claimedOldPlaytime);

        return map;
    }

    public static Reward deserialize(Map<String, Object> map) {
        List<Integer> timeTillNextReward = (List<Integer>) map.getOrDefault("timeTillNextReward", new ArrayList<>());
        int amountRedeemed = (int) map.getOrDefault("amountRedeemed", 0);
        int amountPending = (int) map.getOrDefault("amountPending", 0);
        boolean eligible = (boolean) map.getOrDefault("eligible", true);
        boolean claimedOldPlaytime = (boolean) map.getOrDefault("claimedOldPlaytime", false);

        //old userdata conversion (-1 to empty list)
        if (!timeTillNextReward.isEmpty() && timeTillNextReward.get(0) == -1) {
            eligible = false;
            timeTillNextReward = new ArrayList<>();
        }

        return new Reward(timeTillNextReward, amountRedeemed, amountPending, eligible, claimedOldPlaytime);
    }

    @Override
    public String toString() {
        return "Reward{timeTillNextReward=" + timeTillNextReward + ", amountRedeemed=" + amountRedeemed + ", amountPending=" + amountPending + ", eligible=" + eligible + ", claimedOldPlaytime=" + claimedOldPlaytime + ", cReward=" + cReward + '}';
    }
}
