package com.backtobedrock.rewardslite.domain.placholderAPI;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.domain.data.PlayerData;
import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import com.backtobedrock.rewardslite.domain.enumerations.RewardStatus;
import com.backtobedrock.rewardslite.domain.enumerations.TimePattern;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;

public class PlaceholdersRewardsLite extends PlaceholderExpansion {
    private final Rewardslite plugin;

    public PlaceholdersRewardsLite() {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
    }

    @Override
    public @Nullable String getRequiredPlugin() {
        return this.plugin.getDescription().getName();
    }

    @Override
    public boolean canRegister() {
        return this.plugin != null;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (player.getPlayer() != null) {
            Player onlinePlayer = player.getPlayer();
            PlayerData playerData = this.plugin.getPlayerRepository().getByPlayerSync(player);
            if (playerData != null) {
                MinecraftVersion minecraftVersion = MinecraftVersion.get();

                //next reward time
                if (Arrays.asList("next_reward_time_long", "next_reward_time_short", "next_reward_time_digital").contains(identifier)) {
                    RewardData nextRewardDataTime = playerData.getRewards().stream()
                            .filter(r -> RewardStatus.getRewardStatus(r, player.getPlayer()) == RewardStatus.ELIGIBLE && r.isActive())
                            .min(Comparator.comparingLong(RewardData::getTimeLeft))
                            .orElse(null);
                    switch (identifier) {
                        case "next_reward_time_long":
                            return nextRewardDataTime != null ? MessageUtils.getTimeFromTicks(nextRewardDataTime.getTimeLeft(), TimePattern.LONG) : "-";
                        case "next_reward_time_short":
                            return nextRewardDataTime != null ? MessageUtils.getTimeFromTicks(nextRewardDataTime.getTimeLeft(), TimePattern.SHORT) : "-";
                        case "next_reward_time_digital":
                            return nextRewardDataTime != null ? MessageUtils.getTimeFromTicks(nextRewardDataTime.getTimeLeft(), TimePattern.DIGITAL) : "-";
                    }
                }

                //playtime
                if (Arrays.asList("playtime_long", "playtime_short", "playtime_digital").contains(identifier)) {
                    long playtime = plugin.getConfigurations().getGeneralConfiguration().isCountPreviousTowardsPlaytime()
                            ? Math.min(onlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE) - playerData.getAfkTime(), onlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE))
                            : playerData.getPlaytime();
                    switch (identifier) {
                        case "playtime_long":
                            return MessageUtils.getTimeFromTicks(playtime, TimePattern.LONG);
                        case "playtime_short":
                            return MessageUtils.getTimeFromTicks(playtime, TimePattern.SHORT);
                        case "playtime_digital":
                            return MessageUtils.getTimeFromTicks(playtime, TimePattern.DIGITAL);
                    }
                }

                //total playtime
                if (Arrays.asList("total_playtime_long", "total_playtime_short", "total_playtime_digital").contains(identifier)) {
                    long totalPlaytime = minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_13)
                            ? onlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE)
                            : playerData.getRawPlaytime();
                    switch (identifier) {
                        case "total_playtime_long":
                            return MessageUtils.getTimeFromTicks(totalPlaytime, TimePattern.LONG);
                        case "total_playtime_short":
                            return MessageUtils.getTimeFromTicks(totalPlaytime, TimePattern.SHORT);
                        case "total_playtime_digital":
                            return MessageUtils.getTimeFromTicks(totalPlaytime, TimePattern.DIGITAL);
                    }
                }

                //miscellaneous
                switch (identifier) {
                    case "next_reward_title":
                        RewardData nextRewardDataTitle = playerData.getRewards().stream()
                                .filter(r -> RewardStatus.getRewardStatus(r, player.getPlayer()) == RewardStatus.ELIGIBLE)
                                .min(Comparator.comparingLong(RewardData::getTimeLeft))
                                .orElse(null);
                        return nextRewardDataTitle != null ? nextRewardDataTitle.getDisplay().getName() : "-";
                    case "afk_time_long":
                        return MessageUtils.getTimeFromTicks(playerData.getAfkTime(), TimePattern.LONG);
                    case "afk_time_short":
                        return MessageUtils.getTimeFromTicks(playerData.getAfkTime(), TimePattern.SHORT);
                    case "afk_time_digital":
                        return MessageUtils.getTimeFromTicks(playerData.getAfkTime(), TimePattern.DIGITAL);
                }
            }
        }
        return null;
    }

    @Override
    public @NotNull String getIdentifier() {
        return this.plugin.getDescription().getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return this.plugin.getDescription().getName();
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }
}
