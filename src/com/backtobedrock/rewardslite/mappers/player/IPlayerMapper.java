package com.backtobedrock.rewardslite.mappers.player;

import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.domain.data.PlayerData;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IPlayerMapper {
    //Read
    CompletableFuture<PlayerData> getByPlayer(OfflinePlayer player);

    PlayerData getByPlayerSync(OfflinePlayer player);

    CompletableFuture<List<PlayerData>> getAll();

    List<PlayerData> getAllSync();

    //Update
    void updatePlayerData(PlayerData data);

    void updateRewardData(OfflinePlayer player, RewardData rewardData);

    //Delete
    void deletePlayerData(OfflinePlayer player);
}
