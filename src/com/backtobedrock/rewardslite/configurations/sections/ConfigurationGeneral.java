package com.backtobedrock.rewardslite.configurations.sections;

import com.backtobedrock.rewardslite.domain.enumerations.RewardStatus;
import com.backtobedrock.rewardslite.domain.enumerations.TimeUnit;
import com.backtobedrock.rewardslite.utilities.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationGeneral {
    private final boolean countPreviousTowardsPlaytime;
    private final List<RewardStatus> includedRewards;
    //    private final List<Predicate<?>> rewardsOrder;
    private final int topCommandsLimit;
    private final TimeUnit timeUnitLimit;

    public ConfigurationGeneral(boolean countPreviousTowardsPlaytime, List<RewardStatus> includedRewards, int topCommandsLimit, TimeUnit timeUnitLimit) {
        this.countPreviousTowardsPlaytime = countPreviousTowardsPlaytime;
        this.includedRewards = includedRewards;
        this.topCommandsLimit = topCommandsLimit;
        this.timeUnitLimit = timeUnitLimit;
    }

    public static ConfigurationGeneral deserialize(ConfigurationSection section) {
        boolean cCountPreviousTowardsPlaytime = section.getBoolean("countPreviousTowardsPlaytime", true);
        List<RewardStatus> cIncludedRewards = section.getStringList("includedRewards").stream().map(s -> ConfigUtils.getRewardStatus("includedRewards", s)).collect(Collectors.toList());
//        List<Predicate<?>> cRewardsOrder = section.getStringList("rewardsOrder");
        int cTopCommandLimit = section.getInt("topCommandsLimit", 10);
        TimeUnit cTimeUnitLimit = ConfigUtils.getTimeUnit("timeUnitLimit", section.getString("timeUnitLimit", "DAYS"));

        if (cTimeUnitLimit == null) {
            return null;
        }

        return new ConfigurationGeneral(cCountPreviousTowardsPlaytime, cIncludedRewards, cTopCommandLimit, cTimeUnitLimit);
    }

    public boolean isCountPreviousTowardsPlaytime() {
        return countPreviousTowardsPlaytime;
    }

    public List<RewardStatus> getIncludedRewards() {
        return includedRewards;
    }

    public int getTopCommandsLimit() {
        return topCommandsLimit;
    }

    public TimeUnit getTimeUnitLimit() {
        return timeUnitLimit;
    }
}