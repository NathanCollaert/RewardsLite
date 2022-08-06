package com.backtobedrock.rewardslite.repositories;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.domain.data.PlayerData;
import com.backtobedrock.rewardslite.domain.enumerations.StorageType;
import com.backtobedrock.rewardslite.mappers.player.IPlayerMapper;
import com.backtobedrock.rewardslite.mappers.player.MySQLPlayerMapper;
import com.backtobedrock.rewardslite.mappers.player.YAMLPlayerMapper;
import com.backtobedrock.rewardslite.runnables.CacheClear;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerRepository {
    private final Rewardslite plugin;

    private IPlayerMapper mapper;

    private final Map<UUID, PlayerData> playerCache;
    private final Map<UUID, CacheClear> playerCacheClear;

    public PlayerRepository() {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.playerCache = new ConcurrentHashMap<>();
        this.playerCacheClear = new ConcurrentHashMap<>();
        this.initializeMapper();
    }

    public void onReload() {
        this.initializeMapper();
        this.plugin.getServer().getOnlinePlayers().stream().filter(p -> this.playerCache.containsKey(p.getUniqueId())).forEach(p -> this.playerCache.get(p.getUniqueId()).onReload(p));
    }

    private void initializeMapper() {
        this.setMapper(this.plugin.getConfigurations().getDataConfiguration().getStorageType());
    }

    private void setMapper(StorageType storageType) {
        switch (storageType) {
            case MYSQL:
                this.mapper = MySQLPlayerMapper.getInstance();
                break;
            case YAML:
                this.mapper = new YAMLPlayerMapper();
                break;
        }
    }

    public CompletableFuture<PlayerData> getByPlayer(OfflinePlayer player) {
        if (!doesCacheContainPlayer(player.getUniqueId())) {
            return this.mapper.getByPlayer(player)
                    .thenApplyAsync(playerData -> this.getFromDataAndCache(player, playerData))
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        } else {
            return CompletableFuture.supplyAsync(() -> player)
                    .thenApplyAsync(this::getFromCache)
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    public PlayerData getByPlayerSync(OfflinePlayer player) {
        if (!doesCacheContainPlayer(player.getUniqueId())) {
            return this.getFromDataAndCache(player, this.mapper.getByPlayerSync(player));
        } else {
            return this.getFromCache(player);
        }
    }

    private PlayerData getFromDataAndCache(OfflinePlayer player, PlayerData playerData) {
        if (playerData == null) {
            playerData = new PlayerData(player);
            if (player.hasPlayedBefore() || player.isOnline()) {
                this.mapper.updatePlayerData(playerData);
            }
        }
        this.playerCache.put(player.getUniqueId(), playerData);

        CacheClear cacheClear = this.playerCacheClear.get(player.getUniqueId());
        if (cacheClear != null && player.isOnline()) {
            cacheClear.stop();
            this.playerCacheClear.remove(player.getUniqueId());
        } else if (cacheClear == null) {
            cacheClear = new CacheClear(player, 2400);
            this.playerCacheClear.put(player.getUniqueId(), cacheClear);
            cacheClear.start();
        }

        return playerData;
    }

    private PlayerData getFromCache(OfflinePlayer player) {
        return this.playerCache.get(player.getUniqueId());
    }

    public void updatePlayerData(PlayerData data) {
        this.mapper.updatePlayerData(data);
    }

    public void updateRewardData(OfflinePlayer player, RewardData rewardData) {
        this.mapper.updateRewardData(player, rewardData);
    }

    public void deletePlayerData(OfflinePlayer player) {
        this.mapper.deletePlayerData(player);
    }

    public void queueCacheClear(OfflinePlayer player) {
        CacheClear cacheClear = new CacheClear(player, 6000);
        this.playerCacheClear.put(player.getUniqueId(), cacheClear);
        cacheClear.start();
    }

    public void removeFromPlayerCache(UUID uuid) {
        this.playerCache.remove(uuid);
        this.playerCacheClear.remove(uuid);
    }

    public boolean doesCacheContainPlayer(UUID uuid) {
        return this.playerCache.containsKey(uuid);
    }

    public void setInPlayerCache(PlayerData playerData) {
        PlayerData playerDataCached = this.playerCache.get(playerData.getPlayer().getUniqueId());
        if (playerDataCached != null && playerDataCached.getPlaytimeRunnable() != null) {
            playerDataCached.getPlaytimeRunnable().stop();
        }
        this.playerCache.put(playerData.getPlayer().getUniqueId(), playerData);
    }

    public CompletableFuture<List<PlayerData>> getAll() {
        return this.mapper.getAll();
    }

    public List<PlayerData> getAllSync() {
        return this.mapper.getAllSync();
    }
}
