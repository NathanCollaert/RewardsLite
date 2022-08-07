package com.backtobedrock.rewardslite.mappers.player;

import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.domain.data.PlayerData;
import org.bukkit.OfflinePlayer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IPlayerMapper {
    //Read
    CompletableFuture<PlayerData> getByPlayer(OfflinePlayer player);

    PlayerData getByPlayerSync(OfflinePlayer player);

    CompletableFuture<List<PlayerData>> getAll();

    List<PlayerData> getAllSync();

    CompletableFuture<Map<String, Long>> getTopPlaytime(int limit);

    CompletableFuture<Map<String, Long>> getTopTotalTime(int limit);

    CompletableFuture<Map<String, Long>> getTopAfkTime(int limit);

    Map<String, Long> getTopPlaytimeSync(int limit);

    Map<String, Long> getTopTotalTimeSync(int limit);

    Map<String, Long> getTopAfkTimeSync(int limit);

    //Update
    void updatePlayerData(PlayerData data);

    void updateRewardData(OfflinePlayer player, RewardData rewardData);

    //Delete
    void deletePlayerData(OfflinePlayer player);
}