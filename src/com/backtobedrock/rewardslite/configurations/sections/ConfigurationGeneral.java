package com.backtobedrock.rewardslite.configurations.sections;

import com.backtobedrock.rewardslite.domain.enumerations.RewardStatus;
import com.backtobedrock.rewardslite.utilities.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationGeneral {
    private final boolean countPreviousTowardsPlaytime;
    private final List<RewardStatus> includedRewards;
//    private final List<Predicate<?>> rewardsOrder;

    public ConfigurationGeneral(boolean countPreviousTowardsPlaytime, List<RewardStatus> includedRewards) {
        this.countPreviousTowardsPlaytime = countPreviousTowardsPlaytime;
        this.includedRewards = includedRewards;
    }

    public static ConfigurationGeneral deserialize(ConfigurationSection section) {
        boolean cCountPreviousTowardsPlaytime = section.getBoolean("countPreviousTowardsPlaytime", true);
        List<RewardStatus> cIncludedRewards = section.getStringList("includedRewards").stream().map(s -> ConfigUtils.getRewardStatus("includedRewards", s)).collect(Collectors.toList());
//        List<Predicate<?>> cRewardsOrder = section.getStringList("rewardsOrder");
        return new ConfigurationGeneral(cCountPreviousTowardsPlaytime, cIncludedRewards);
    }

    public boolean isCountPreviousTowardsPlaytime() {
        return countPreviousTowardsPlaytime;
    }

    public List<RewardStatus> getIncludedRewards() {
        return includedRewards;
    }
}
