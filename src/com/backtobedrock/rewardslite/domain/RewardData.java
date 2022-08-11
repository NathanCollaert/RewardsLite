package com.backtobedrock.rewardslite.domain;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.data.PlayerData;
import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import com.backtobedrock.rewardslite.domain.enumerations.TimePattern;
import com.backtobedrock.rewardslite.domain.observer.IObserver;
import com.backtobedrock.rewardslite.domain.observer.RewardObserver;
import com.backtobedrock.rewardslite.interfaces.AbstractInterface;
import com.backtobedrock.rewardslite.interfaces.InterfaceRewards;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public class RewardData extends Reward {
    private final Rewardslite plugin;

    //Data
    private long timeLeft;
    private int redeemed;
    private int pending;
    private boolean active;
    private boolean countedPrevious;

    //helpers
    private final Map<UUID, Map<Class<?>, IObserver>> observers;

    public RewardData(Reward reward) {
        this(reward, reward.getRequiredTime() * 1200L, 0, 0, true, false);
    }

    public RewardData(Reward reward, long timeLeft, int redeemed, int pending, boolean active, boolean countedPrevious) {
        super(reward);
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.timeLeft = Math.min(timeLeft, super.getRequiredTime() * 1200L);
        this.redeemed = redeemed;
        this.pending = pending;
        this.active = super.isLoop() || (active && redeemed <= 0);
        this.countedPrevious = countedPrevious;
        this.observers = new HashMap<>();
    }

    public RewardData(Reward reward, RewardData rewardData) {
        this(reward, rewardData.getTimeLeft(), rewardData.getRedeemed(), rewardData.getPending(), rewardData.isActive(), rewardData.hasCountedPrevious());
    }

    public long getTimeLeft() {
        return timeLeft;
    }

    public int getRedeemed() {
        return redeemed;
    }

    public int getPending() {
        return pending;
    }

    public ItemStack getDisplayItem(Player player) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("total_time", MessageUtils.getTimeFromTicks(getRequiredTime() * 1200L, TimePattern.LONG));
            put("time_left", MessageUtils.getTimeFromTicks(timeLeft, TimePattern.LONG));
            put("redeemed", Integer.toString(redeemed));
            put("pending", Integer.toString(pending));
        }};
        Display displayItem = this.pending > 0 ? this.getDisplayPending() : !this.hasPermission(player) ? this.getDisplayIneligible() : this.isActive() ? this.getDisplay() : this.getDisplayClaimed();
        return displayItem.getItem(placeholders);
    }

    public void countPrevious(Player player, PlayerData playerData) {
        if (!getAvailabilityPredicate(player).test(this)) {
            return;
        }

        if (this.hasCountedPrevious()) {
            return;
        }

        long time = 0;
        switch (this.getCountPrevious()) {
            case ALL:
                MinecraftVersion minecraftVersion = MinecraftVersion.get();
                time = minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_13) ? player.getStatistic(Statistic.PLAY_ONE_MINUTE) : playerData.getRawPlaytime();
                break;
            case PLUGIN:
                time += (playerData.getPlaytime() + (this.isCountAfk() ? playerData.getAfkTime() : 0));
                break;
        }
        this.decreaseTimeLeft(time, player, false);
        this.setCountedPrevious(true);
        this.observers.forEach((key, value) -> value.get(RewardObserver.class).update());
    }

    public boolean decreaseTimeLeft(long amount, Player player, boolean ignorePredicate) {
        if (!ignorePredicate && !getAvailabilityPredicate(player).test(this)) {
            return false;
        }

        boolean altered = false;
        this.timeLeft -= amount;
        while (this.timeLeft <= 0 && (ignorePredicate || this.active)) {
            if (super.isManualClaim() || !super.redeemReward(player, false)) {
                this.pending++;
                this.getPendingNotification().getNotifications().forEach(n -> n.notify(player, player.getName()));
            } else {
                this.redeemed++;
            }

            if (!super.isLoop() || this.getRedeemed() >= super.getMaximumAmountRedeemed()) {
                this.active = false;
            }
            this.timeLeft = (ignorePredicate || this.active) ? this.timeLeft + super.getRequiredTime() * 1200L : super.getRequiredTime() * 1200L;
            altered = true;
        }

        this.observers.forEach((key, value) -> value.get(RewardObserver.class).update());

        return altered;
    }

    public void claimPending(Player player) {
        if (this.pending <= 0) {
            return;
        }

        Bukkit.getScheduler().runTask(this.plugin, () -> {
            if (super.redeemReward(player, true)) {
                this.pending--;
                this.redeemed++;
                this.plugin.getPlayerRepository().updateRewardData(player, this);
                this.observers.forEach((key, value) -> value.get(RewardObserver.class).update());
            }
        });
    }

    public void registerObserver(Player player, AbstractInterface iface) {
        Map<Class<?>, IObserver> observers = new HashMap<>();
        if (iface instanceof InterfaceRewards) {
            InterfaceRewards interfaceRewards = (InterfaceRewards) iface;
            Collections.singletonList(
                    new RewardObserver(interfaceRewards, this)
            ).forEach(e -> observers.put(e.getClass(), e));
        }
        if (!observers.isEmpty()) {
            this.observers.put(player.getUniqueId(), observers);
        }
    }

    public void unregisterObserver(Player player) {
        this.observers.remove(player.getUniqueId());
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean hasCountedPrevious() {
        return countedPrevious;
    }

    public static Predicate<RewardData> getAvailabilityPredicate(Player player) {
        return rewardData -> rewardData.isActive() && rewardData.hasPermission(player) && rewardData.getPending() < rewardData.getMaximumAmountPending();
    }

    public void setCountedPrevious(boolean countedPrevious) {
        this.countedPrevious = countedPrevious;
    }

    public void reset() {
        this.timeLeft = super.getRequiredTime() * 1200L;
        this.redeemed = 0;
        this.pending = 0;
        this.active = true;
        this.countedPrevious = false;
    }
}
