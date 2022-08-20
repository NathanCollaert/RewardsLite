package com.backtobedrock.rewardslite.mappers.player;

import com.backtobedrock.rewardslite.domain.Reward;
import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.domain.data.PlayerData;
import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import com.backtobedrock.rewardslite.mappers.AbstractMapper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class MySQLPlayerMapper extends AbstractMapper implements IPlayerMapper {
    private static MySQLPlayerMapper instance;

    public static MySQLPlayerMapper getInstance() {
        if (instance == null) {
            instance = new MySQLPlayerMapper();
        }
        return instance;
    }

    @Override
    public CompletableFuture<PlayerData> getByPlayer(OfflinePlayer player) {
        return CompletableFuture
                .supplyAsync(() -> this.getPlayerData(player))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    @Override
    public PlayerData getByPlayerSync(OfflinePlayer player) {
        return this.getPlayerData(player);
    }

    @Override
    public CompletableFuture<List<PlayerData>> getAll() {
        return CompletableFuture.supplyAsync(this::getAllSync)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    @Override
    public List<PlayerData> getAllSync() {
        try (Connection connection = this.database.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(this.getAllSQL())) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<PlayerData> playerDataList = new ArrayList<>();
            OfflinePlayer player = null;
            PlayerData playerData = null;
            List<RewardData> rewards = new ArrayList<>();
            while (resultSet.next()) {
                if (player != null && !player.getUniqueId().equals(UUID.fromString(resultSet.getString("player_uuid")))) {
                    this.plugin.getRewardsRepository().getAll().stream()
                            .filter(r -> !rewards.stream()
                                    .map(RewardData::getUuid)
                                    .collect(Collectors.toList()).contains(r.getUuid()))
                            .forEach(r -> rewards.add(new RewardData(r)));
                    playerData.setRewards(rewards);
                    playerDataList.add(playerData);
                    player = null;
                    playerData = null;
                    rewards.clear();
                }
                if (player == null) {
                    player = this.plugin.getServer().getOfflinePlayer(UUID.fromString(resultSet.getString("player_uuid")));
                }
                if (playerData == null) {
                    playerData = new PlayerData(player, resultSet.getLong("playtime"), resultSet.getLong("afk_time"), new ArrayList<>());
                }
                String uuid = resultSet.getString("reward_uuid");
                Reward reward = uuid == null ? null : this.plugin.getRewardsRepository().getById(UUID.fromString(uuid));
                if (reward != null) {
                    rewards.add(new RewardData(reward, resultSet.getLong("time_left"), resultSet.getInt("redeemed"), resultSet.getInt("pending"), resultSet.getBoolean("active"), resultSet.getBoolean("counted_previous")));
                } else {
                    this.deleteRewardData(player.getUniqueId(), resultSet.getString("reward_uuid"));
                }
            }
            return playerDataList;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public CompletableFuture<Map<String, Long>> getTopPlaytime(int limit) {
        return CompletableFuture.supplyAsync(() -> this.getTopPlaytimeSync(limit))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    @Override
    public CompletableFuture<Map<String, Long>> getTopTotalTime(int limit) {
        return CompletableFuture.supplyAsync(() -> this.getTopTotalTimeSync(limit))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    @Override
    public CompletableFuture<Map<String, Long>> getTopAfkTime(int limit) {
        return CompletableFuture.supplyAsync(() -> this.getTopAfkTimeSync(limit))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    @Override
    public Map<String, Long> getTopPlaytimeSync(int limit) {
        try (Connection connection = this.database.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(this.getTopPlaytime())) {
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Long> topMap = new LinkedHashMap<>();
            while (resultSet.next()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("player_uuid")));
                MinecraftVersion minecraftVersion = MinecraftVersion.get();
                long playtime = this.plugin.getConfigurations().getGeneralConfiguration().isCountPreviousTowardsPlaytime() && minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_16) ? player.getStatistic(Statistic.PLAY_ONE_MINUTE) - resultSet.getLong("afk_time") : resultSet.getLong("playtime");
                topMap.put(player.getName() == null ? resultSet.getString("player_uuid") : player.getName(), playtime);
            }
            return getSortedMap(limit, topMap);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<String, Long> getTopTotalTimeSync(int limit) {
        try (Connection connection = this.database.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(this.getTopTotalTime())) {
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Long> topMap = new LinkedHashMap<>();
            while (resultSet.next()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("player_uuid")));
                MinecraftVersion minecraftVersion = MinecraftVersion.get();
                long totalTime = this.plugin.getConfigurations().getGeneralConfiguration().isCountPreviousTowardsPlaytime() && minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_16) ? player.getStatistic(Statistic.PLAY_ONE_MINUTE) : resultSet.getLong("playtime") + resultSet.getLong("afk_time");
                topMap.put(player.getName() == null ? resultSet.getString("player_uuid") : player.getName(), totalTime);
            }
            return getSortedMap(limit, topMap);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map<String, Long> getTopAfkTimeSync(int limit) {
        try (Connection connection = this.database.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(this.getTopAfkTime())) {
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Long> topMap = new LinkedHashMap<>();
            while (resultSet.next()) {
                String playerName = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("player_uuid"))).getName();
                topMap.put(playerName == null ? resultSet.getString("player_uuid") : playerName, resultSet.getLong("afk_time"));
            }
            return getSortedMap(limit, topMap);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Map<String, Long> getSortedMap(int limit, Map<String, Long> map) {
        List<Map.Entry<String, Long>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        Map<String, Long> topMapLimit = new LinkedHashMap<>();
        int limitPosition = 1;
        for (Map.Entry<String, Long> entry : list) {
            topMapLimit.put(entry.getKey(), entry.getValue());
            if (limitPosition == limit) {
                break;
            }
            limitPosition++;
        }
        return topMapLimit;
    }

    private PlayerData getPlayerData(OfflinePlayer player) {
        try (Connection connection = this.database.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(this.getPlayerSQL())) {
            preparedStatement.setString(1, player.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            PlayerData playerData = null;
            List<RewardData> rewards = new ArrayList<>();
            while (resultSet.next()) {
                if (playerData == null) {
                    playerData = new PlayerData(player, resultSet.getLong("playtime"), resultSet.getLong("afk_time"), new ArrayList<>());
                }
                String uuid = resultSet.getString("reward_uuid");
                Reward reward = uuid == null ? null : this.plugin.getRewardsRepository().getById(UUID.fromString(uuid));
                if (reward != null) {
                    rewards.add(new RewardData(reward, resultSet.getLong("time_left"), resultSet.getInt("redeemed"), resultSet.getInt("pending"), resultSet.getBoolean("active"), resultSet.getBoolean("counted_previous")));
                } else {
                    this.deleteRewardData(player.getUniqueId(), resultSet.getString("reward_uuid"));
                }
            }
            if (playerData != null) {
                this.plugin.getRewardsRepository().getAll().stream()
                        .filter(r -> !rewards.stream()
                                .map(RewardData::getUuid)
                                .collect(Collectors.toList()).contains(r.getUuid()))
                        .forEach(r -> rewards.add(new RewardData(r)));
                playerData.setRewards(rewards);
            }
            return playerData;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updatePlayerData(PlayerData playerData) {
        if (!this.plugin.isEnabled()) {
            this.upsertPlayerData(playerData);
        } else {
            CompletableFuture.runAsync(() -> this.upsertPlayerData(playerData))
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    @Override
    public void updateRewardData(OfflinePlayer player, RewardData rewardData) {
        if (!this.plugin.isEnabled()) {
            this.upsertRewardData(player.getUniqueId(), rewardData);
        } else {
            CompletableFuture.runAsync(() -> this.upsertRewardData(player.getUniqueId(), rewardData))
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    private void upsertPlayerData(PlayerData playerData) {
        try (Connection connection = this.database.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(this.upsertPlayerSQL())) {
            preparedStatement.setString(1, playerData.getPlayer().getUniqueId().toString());
            preparedStatement.setString(2, playerData.getPlayer().getName());
            preparedStatement.setLong(3, playerData.getPlaytime());
            preparedStatement.setLong(4, playerData.getAfkTime());
            preparedStatement.setString(5, playerData.getPlayer().getName());
            preparedStatement.setLong(6, playerData.getPlaytime());
            preparedStatement.setLong(7, playerData.getAfkTime());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        playerData.getRewards().forEach(r -> this.upsertRewardData(playerData.getPlayer().getUniqueId(), r));
    }

    private void upsertRewardData(UUID playerUuid, RewardData rewardData) {
        try (Connection connection = this.database.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(this.upsertRewardDataSQL())) {
            preparedStatement.setString(1, rewardData.getUuid().toString());
            preparedStatement.setString(2, playerUuid.toString());
            preparedStatement.setLong(3, rewardData.getTimeLeft());
            preparedStatement.setInt(4, rewardData.getRedeemed());
            preparedStatement.setInt(5, rewardData.getPending());
            preparedStatement.setBoolean(6, rewardData.isActive());
            preparedStatement.setBoolean(7, rewardData.hasCountedPrevious());
            preparedStatement.setLong(8, rewardData.getTimeLeft());
            preparedStatement.setInt(9, rewardData.getRedeemed());
            preparedStatement.setInt(10, rewardData.getPending());
            preparedStatement.setBoolean(11, rewardData.isActive());
            preparedStatement.setBoolean(12, rewardData.hasCountedPrevious());
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deletePlayerData(OfflinePlayer player) {
        CompletableFuture.runAsync(() -> {
                    try (Connection connection = this.database.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(this.deletePlayerSQL())) {
                        preparedStatement.setString(1, player.getUniqueId().toString());
                        preparedStatement.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    public void deleteRewardData(UUID playerUuid, String rewardUuid) {
        CompletableFuture.runAsync(() -> {
                    try (Connection connection = this.database.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(this.deleteRewardSQL())) {
                        preparedStatement.setString(1, rewardUuid);
                        preparedStatement.setString(2, playerUuid.toString());
                        preparedStatement.execute();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private String getPlayerSQL() {
        return "SELECT p.*, r.reward_uuid, r.time_left, r.redeemed, r.pending, r.active, r.counted_previous "
                + "FROM rl_player AS p "
                + "LEFT JOIN rl_reward AS r ON p.player_uuid = r.player_uuid "
                + "WHERE p.player_uuid = ?;";
    }

    private String getAllSQL() {
        return "SELECT p.*, r.reward_uuid, r.time_left, r.redeemed, r.pending, r.active, r.counted_previous "
                + "FROM rl_player AS p "
                + "LEFT JOIN rl_reward AS r ON p.player_uuid = r.player_uuid;";
    }

    private String getTopPlaytime() {
        return "SELECT player_uuid, playtime, afk_time "
                + "FROM rl_player;";
    }

    private String getTopTotalTime() {
        return "SELECT player_uuid, playtime, afk_time "
                + "FROM rl_player;";
    }

    private String getTopAfkTime() {
        return "SELECT player_uuid, afk_time "
                + "FROM rl_player;";
    }

    private String deletePlayerSQL() {
        return "DELETE FROM rl_player " +
                "WHERE player_uuid = ?;";
    }

    private String deleteRewardSQL() {
        return "DELETE FROM rl_reward " +
                "WHERE reward_uuid = ? AND player_uuid = ?;";
    }

    private String upsertPlayerSQL() {
        return "INSERT INTO rl_player (`player_uuid`, `last_known_name`, `playtime`, `afk_time`)"
                + "VALUES(?,?,?,?)"
                + "ON DUPLICATE KEY UPDATE "
                + "`last_known_name` = ?,"
                + "`playtime` = ?,"
                + "`afk_time` = ?;";
    }

    private String upsertRewardDataSQL() {
        return "INSERT INTO rl_reward (`reward_uuid`, `player_uuid`, `time_left`, `redeemed`, `pending`, `active`, `counted_previous`)"
                + "VALUES(?,?,?,?,?,?,?)"
                + "ON DUPLICATE KEY UPDATE "
                + "`time_left` = ?,"
                + "`redeemed` = ?,"
                + "`pending` = ?,"
                + "`active` = ?,"
                + "`counted_previous` = ?;";
    }
}