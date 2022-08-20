package com.backtobedrock.rewardslite.repositories;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.Reward;
import com.backtobedrock.rewardslite.mappers.reward.IRewardMapper;
import com.backtobedrock.rewardslite.mappers.reward.YAMLRewardMapper;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class RewardRepository {
    private final Rewardslite plugin;
    private IRewardMapper mapper;

    private List<Reward> rewards = null;

    public RewardRepository() {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.initializeMapper();
        if (this.getAll() != null && this.plugin.isEnabled()) {
            this.plugin.getLogger().log(Level.INFO, "Loaded {0} {1}.", new Object[]{this.getAll().size(), this.getAll().size() == 1 ? "reward" : "rewards"});
        }
    }

    private void initializeMapper() {
        this.mapper = new YAMLRewardMapper();
    }

    public List<Reward> getAll() {
        if (this.rewards == null) {
            this.rewards = this.mapper.getAll();
        }
        return this.rewards;
    }

    public Reward getById(UUID uuid) {
        return this.getAll().stream().filter(reward -> reward.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public Reward getByFileName(String fileName) {
        return this.getAll().stream().filter(reward -> reward.getFileName().equalsIgnoreCase(fileName)).findFirst().orElse(null);
    }

    public Reward getByPermission(String permission) {
        return this.getAll().stream().filter(reward -> reward.getPermissionId().equals(permission.toLowerCase())).findFirst().orElse(null);
    }

    public void updateReward(Reward reward) {
        this.mapper.upsertReward(reward);
    }

    public void clearRewardsCache() {
        this.rewards = null;
    }

    public void onReload() {
        this.clearRewardsCache();
    }
}
