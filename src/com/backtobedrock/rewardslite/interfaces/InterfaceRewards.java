package com.backtobedrock.rewardslite.interfaces;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.CustomHolder;
import com.backtobedrock.rewardslite.domain.Icon;
import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.domain.data.PlayerData;
import com.backtobedrock.rewardslite.interfaces.clickActions.AbstractClickAction;
import com.backtobedrock.rewardslite.interfaces.clickActions.ClickActionRedeemReward;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InterfaceRewards extends AbstractPaginatedInterface {
    private final Player sender;
    private final OfflinePlayer target;
    private final PlayerData playerData;
    private Map<UUID, Integer> rewards;
    private final List<RewardData> viewableRewards;

    public InterfaceRewards(Player sender, OfflinePlayer target, PlayerData playerData) {
        super(new CustomHolder(playerData.getViewableRewards(target).size(), JavaPlugin.getPlugin(Rewardslite.class).getConfigurations().getInterfacesConfiguration().getRewardsInterfaceTitle(playerData.getPlayer().getName())), playerData.getViewableRewards(target).size());
        this.sender = sender;
        this.target = target;
        this.playerData = playerData;
        this.viewableRewards = playerData.getViewableRewards(target);
        this.initialize();
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.setInterfaceAccent();
        this.setData();
    }

    private void setInterfaceAccent() {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < this.customHolder.getSize(); i++) {
            if (i == 1 || i == 7 || i == 4 || i == (this.customHolder.getRowAmount() - 1) * 9 + 4 || i == 9 || i == 17 || i == (this.customHolder.getRowAmount() - 1) * 9 + 1 || i == (this.customHolder.getRowAmount() - 1) * 9 + 7 || i == ((this.customHolder.getRowAmount() - 2) * 9) || i == ((this.customHolder.getRowAmount() - 2) * 9) + 8) {
                positions.add(i);
            }
        }
        this.setAccentColor(positions);
    }

    @Override
    protected void setData() {
        super.setData();
        this.updateRewards();
    }

    private void updateRewards() {
        this.rewards = new ConcurrentHashMap<>();
        List<RewardData> currentData = this.viewableRewards.subList((this.currentPage - 1) * 28, Math.min(this.currentPage * 28, this.viewableRewards.size()));

        currentData.forEach(e -> this.rewards.put(e.getUuid(), this.customHolder.addIcon(this.getRewardIcon(e))));
    }

    public void updateReward(boolean update, RewardData rewardData) {
        if (this.rewards.containsKey(rewardData.getUuid())) {
            this.customHolder.setIcon(this.rewards.get(rewardData.getUuid()), getRewardIcon(rewardData), update);
        }
    }

    private Icon getRewardIcon(RewardData rewardData) {
        List<AbstractClickAction> clickActions = new ArrayList<>();
        if (rewardData.getPending() > 0 && this.playerData.getPlayer().getUniqueId().equals(this.sender.getUniqueId())) {
            clickActions.add(new ClickActionRedeemReward(rewardData));
        }
        return new Icon(rewardData.getDisplayItem(this.sender), clickActions);
    }

    @Override
    public Inventory getInventory() {
        this.viewableRewards.forEach(r -> r.registerObserver(this.sender, this));
        this.playerData.registerObserver(this.sender, this);
        return super.getInventory();
    }

    public void unregisterObservers() {
        this.viewableRewards.forEach(r -> r.unregisterObserver(this.sender));
        this.playerData.unregisterObserver(this.sender);
    }

    public Player getSender() {
        return sender;
    }

    public OfflinePlayer getTarget() {
        return target;
    }
}
