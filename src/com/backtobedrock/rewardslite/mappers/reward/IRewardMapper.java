package com.backtobedrock.rewardslite.mappers.reward;

import com.backtobedrock.rewardslite.domain.Reward;

import java.util.List;

public interface IRewardMapper {
    List<Reward> getAll();

    void upsertReward(Reward reward);
}
