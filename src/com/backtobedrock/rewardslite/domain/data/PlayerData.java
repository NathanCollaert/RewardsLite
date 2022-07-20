package com.backtobedrock.rewardslite.domain.data;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.Reward;
import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.domain.enumerations.RewardStatus;
import com.backtobedrock.rewardslite.domain.observer.IObserver;
import com.backtobedrock.rewardslite.domain.observer.RewardsObserver;
import com.backtobedrock.rewardslite.interfaces.AbstractInterface;
import com.backtobedrock.rewardslite.interfaces.InterfaceRewards;
import com.backtobedrock.rewardslite.runnables.Playtime;
import com.earth2me.essentials.User;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerData {
    private final Rewardslite plugin;
    private final OfflinePlayer player;

    //data
    private long playtime;
    private long afkTime;
    private List<RewardData> rewards;

    //dependencies for AFK
    private User user = null;

    //helpers
    private Playtime playtimeRunnable;
    private final Map<UUID, Map<Class<?>, IObserver>> observers;

    public PlayerData(OfflinePlayer player, long playtime, long afkTime, List<RewardData> rewards) {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.player = player;
        this.playtime = playtime;
        this.afkTime = afkTime;
        this.setRewards(rewards);
        if (this.plugin.getEssentials() != null) {
            this.user = this.plugin.getEssentials().getUser(player.getUniqueId());
        }
        this.observers = new HashMap<>();
    }

    public PlayerData(OfflinePlayer player) {
        this(player, 0, 0, JavaPlugin.getPlugin(Rewardslite.class).getRewardsRepository().getAll().stream().map(RewardData::new).collect(Collectors.toList()));
    }

    public OfflinePlayer getPlayer() {
        return player;
    }

    public void onJoin(Player player) {
        this.rewards.forEach(r -> r.countPrevious(player, this));
        if (this.playtimeRunnable != null) {
            this.playtimeRunnable.stop();
        }
        this.playtimeRunnable = new Playtime(player, this);
        this.playtimeRunnable.start();
        this.plugin.getPlayerRepository().updatePlayerData(this);
    }

    public void onReload(Player player) {
        List<RewardData> rewardsData = new ArrayList<>();
        this.plugin.getRewardsRepository().getAll().forEach(r -> {
            RewardData rewardData = this.rewards.stream().filter(rd -> rd.getUuid().equals(r.getUuid())).findFirst().orElse(null);
            rewardsData.add(rewardData != null ? new RewardData(r, rewardData) : new RewardData(r));
        });
        this.setRewards(rewardsData);
        this.onJoin(player);
        this.observers.forEach((key, value) -> value.get(RewardsObserver.class).update());
    }

    public void onQuit() {
        this.playtimeRunnable.stop();
        this.playtimeRunnable = null;
        this.plugin.getPlayerRepository().updatePlayerData(this);
        this.plugin.getPlayerRepository().queueCacheClear(this.getPlayer());
    }

    public void increasePlaytime(int amount) {
        this.playtime += amount;
    }

    public void increaseAfkTime(int amount) {
        this.afkTime += amount;
    }

    /**
     * @return The playtime of the player.
     */
    public long getPlaytime() {
        return playtime;
    }

    /**
     * @return The afk time of the player.
     */
    public long getAfkTime() {
        return afkTime;
    }

    public List<RewardData> getViewableRewards(Player player) {
        return this.rewards.stream()
                .filter(r -> this.plugin.getConfigurations().getGeneralConfiguration().getIncludedRewards().contains(RewardStatus.getRewardStatus(r, player)))
                .sorted(Comparator.comparingInt(RewardData::getPending).reversed()
                        .thenComparing(r -> RewardStatus.getRewardStatus(r, player))
                        .thenComparingLong(RewardData::getTimeLeft)
                        .thenComparing(r -> r.getDisplay().getName()))
                .collect(Collectors.toList());
    }

    /**
     * @return The playtime of the player, afk time included.
     */
    public long getRawPlaytime() {
        return (this.playtime - (this.playtime % 20)) + (this.afkTime - (this.afkTime % 20));
    }

    public boolean isAfk() {
        return (this.user != null && this.user.isAfk());
    }

    public Playtime getPlaytimeRunnable() {
        return this.playtimeRunnable;
    }

    public List<RewardData> getRewards() {
        return this.rewards;
    }

    public void registerObserver(Player player, AbstractInterface iface) {
        Map<Class<?>, IObserver> observers = new HashMap<>();
        if (iface instanceof InterfaceRewards) {
            InterfaceRewards interfaceRewards = (InterfaceRewards) iface;
            Collections.singletonList(
                    new RewardsObserver(interfaceRewards, this)
            ).forEach(e -> observers.put(e.getClass(), e));
        }
        if (!observers.isEmpty()) {
            this.observers.put(player.getUniqueId(), observers);
        }
    }

    public void unregisterObserver(Player player) {
        this.observers.remove(player.getUniqueId());
    }

    public void setRewards(List<RewardData> rewards) {
        this.rewards = Collections.synchronizedList(rewards);
    }

    public RewardData getRewardData(Reward reward) {
        return this.rewards.stream().filter(rewardData -> rewardData.getUuid().equals(reward.getUuid())).findFirst().orElse(null);
    }

    public void resetRewards(List<Reward> rewards) {
        List<UUID> convertedRewards = rewards.stream().map(Reward::getUuid).collect(Collectors.toList());
        this.rewards.stream().filter(r -> convertedRewards.contains(r.getUuid())).forEach(RewardData::reset);
        if (this.plugin.getPlayerRepository().doesCacheContainPlayer(this.getPlayer().getUniqueId())) {
            this.plugin.getPlayerRepository().setInPlayerCache(this);
        }
        this.plugin.getPlayerRepository().updatePlayerData(this);
    }
}
