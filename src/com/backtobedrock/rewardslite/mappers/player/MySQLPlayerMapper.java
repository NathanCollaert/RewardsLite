package com.backtobedrock.rewardslite.mappers.player;

import com.backtobedrock.rewardslite.domain.Reward;
import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.domain.data.PlayerData;
import com.backtobedrock.rewardslite.mappers.AbstractMapper;
import org.bukkit.OfflinePlayer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
        return CompletableFuture
                .supplyAsync(this::getAllSync)
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
        if (this.plugin.isStopping()) {
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
        if (this.plugin.isStopping()) {
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