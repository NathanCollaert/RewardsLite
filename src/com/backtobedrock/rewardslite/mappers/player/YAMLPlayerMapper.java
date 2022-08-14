package com.backtobedrock.rewardslite.mappers.player;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.Reward;
import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.domain.data.PlayerData;
import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YAMLPlayerMapper implements IPlayerMapper {
    private final Rewardslite plugin;

    public YAMLPlayerMapper() {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);

        //create userdata folder if none existent
        File udFile = new File(this.plugin.getDataFolder() + "/userdata");
        if (udFile.mkdirs()) {
            this.plugin.getLogger().log(Level.INFO, "Creating {0}.", udFile.getAbsolutePath());
        }
    }

    @Override
    public CompletableFuture<PlayerData> getByPlayer(OfflinePlayer player) {
        return CompletableFuture
                .supplyAsync(() -> this.getByPlayerSync(player))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    @Override
    public PlayerData getByPlayerSync(OfflinePlayer player) {
        return this.deserialize(player, this.getConfig(player));
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
        List<PlayerData> playerDataList = new ArrayList<>();
        File playerDataDirectory = new File(this.plugin.getDataFolder(), "userdata");
        if (playerDataDirectory.exists()) {
            try (Stream<Path> walk = Files.walk(playerDataDirectory.toPath())) {
                List<Path> result = walk.filter(Files::isRegularFile).collect(Collectors.toList());
                result.forEach(e -> {
                    PlayerData playerData = this.deserialize(this.plugin.getServer().getOfflinePlayer(UUID.fromString(e.getFileName().toString().replaceAll(".yml", ""))), YamlConfiguration.loadConfiguration(e.toFile()));
                    playerDataList.add(playerData);
                });
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, e.getMessage());
            }
        }
        return playerDataList;
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
        Map<String, Long> topMap = new HashMap<>();
        File playerDataDirectory = new File(this.plugin.getDataFolder(), "userdata");
        if (playerDataDirectory.exists()) {
            try (Stream<Path> walk = Files.walk(playerDataDirectory.toPath())) {
                List<Path> result = walk.filter(Files::isRegularFile).collect(Collectors.toList());
                result.forEach(e -> {
                    FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(e.toFile());
                    OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(e.getFileName().toString().replaceAll(".yml", "")));
                    MinecraftVersion minecraftVersion = MinecraftVersion.get();
                    long playtime = this.plugin.getConfigurations().getGeneralConfiguration().isCountPreviousTowardsPlaytime() && minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_16) ? player.getStatistic(Statistic.PLAY_ONE_MINUTE) - fileConfiguration.getLong("afk_time", 0) : fileConfiguration.getLong("playtime", 0);
                    topMap.put(player.getName() == null ? e.getFileName().toString().replaceAll(".yml", "") : player.getName(), playtime);
                });
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, e.getMessage());
            }
        }
        return this.getSortedMap(limit, topMap);
    }

    @Override
    public Map<String, Long> getTopTotalTimeSync(int limit) {
        Map<String, Long> topMap = new HashMap<>();
        File playerDataDirectory = new File(this.plugin.getDataFolder(), "userdata");
        if (playerDataDirectory.exists()) {
            try (Stream<Path> walk = Files.walk(playerDataDirectory.toPath())) {
                List<Path> result = walk.filter(Files::isRegularFile).collect(Collectors.toList());
                result.forEach(e -> {
                    FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(e.toFile());
                    OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(e.getFileName().toString().replaceAll(".yml", "")));
                    MinecraftVersion minecraftVersion = MinecraftVersion.get();
                    long totalTime = this.plugin.getConfigurations().getGeneralConfiguration().isCountPreviousTowardsPlaytime() && minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_16) ? player.getStatistic(Statistic.PLAY_ONE_MINUTE) : fileConfiguration.getLong("playtime", 0) + fileConfiguration.getLong("afk_time", 0);
                    topMap.put(player.getName() == null ? e.getFileName().toString().replaceAll(".yml", "") : player.getName(), totalTime);
                });
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, e.getMessage());
            }
        }
        return this.getSortedMap(limit, topMap);
    }

    @Override
    public Map<String, Long> getTopAfkTimeSync(int limit) {
        Map<String, Long> topMap = new HashMap<>();
        File playerDataDirectory = new File(this.plugin.getDataFolder(), "userdata");
        if (playerDataDirectory.exists()) {
            try (Stream<Path> walk = Files.walk(playerDataDirectory.toPath())) {
                List<Path> result = walk.filter(Files::isRegularFile).collect(Collectors.toList());
                result.forEach(e -> {
                    FileConfiguration fileConfiguration = YamlConfiguration.loadConfiguration(e.toFile());
                    String playerName = Bukkit.getOfflinePlayer(UUID.fromString(e.getFileName().toString().replaceAll(".yml", ""))).getName();
                    topMap.put(playerName == null ? e.getFileName().toString().replaceAll(".yml", "") : playerName, fileConfiguration.getLong("afkTime", 0));
                });
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, e.getMessage());
            }
        }
        return this.getSortedMap(limit, topMap);
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

    @Override
    public void updatePlayerData(PlayerData playerData) {
        if (this.plugin.isStopping()) {
            this.upsertPlayerData(playerData);
        } else {
            CompletableFuture
                    .runAsync(() -> this.upsertPlayerData(playerData))
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    private void upsertPlayerData(PlayerData playerData) {
        FileConfiguration config = this.getConfig(playerData.getPlayer());
        this.serialize(playerData).forEach(config::set);
        this.saveConfig(playerData.getPlayer(), config);
    }

    @Override
    public void updateRewardData(OfflinePlayer player, RewardData rewardData) {
        if (this.plugin.isStopping()) {
            this.upsertReward(player, rewardData);
        } else {
            CompletableFuture
                    .runAsync(() -> this.upsertReward(player, rewardData))
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    private void upsertReward(OfflinePlayer player, RewardData rewardData) {
        FileConfiguration config = this.getConfig(player);
        config.set(String.format("rewards.%s", rewardData.getUuid().toString()), this.serialize(rewardData));
        this.saveConfig(player, config);
    }

    @Override
    public void deletePlayerData(OfflinePlayer player) {
        CompletableFuture
                .runAsync(() -> {
                    File file = this.getFile(player);
                    if (file.exists()) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private void saveConfig(OfflinePlayer player, FileConfiguration config) {
        try {
            config.save(this.getFile(player));
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot save to {0}", this.getFile(player).getName());
        }
    }

    private FileConfiguration getConfig(OfflinePlayer player) {
        return YamlConfiguration.loadConfiguration(this.getFile(player));
    }

    private File getFile(OfflinePlayer player) {
        return new File(this.plugin.getDataFolder(), "/userdata/" + player.getUniqueId() + ".yml");
    }

    private PlayerData deserialize(OfflinePlayer player, ConfigurationSection configurationSection) {
        long cPlaytime = configurationSection.getLong("playtime", 0);
        long cAfkTime = configurationSection.getLong("afkTime", 0);
        List<RewardData> cRewards = new ArrayList<>();

        this.plugin.getRewardsRepository().getAll().forEach(r -> {
            ConfigurationSection rewardSection = configurationSection.getConfigurationSection(String.format("rewards.%s", r.getUuid().toString()));
            cRewards.add(rewardSection != null ? this.deserialize(r, rewardSection) : new RewardData(r));
        });

        return new PlayerData(player, cPlaytime, cAfkTime, cRewards);
    }

    private RewardData deserialize(Reward reward, ConfigurationSection configurationSection) {
        long cTimeLeft = configurationSection.getLong("timeLeft", reward.getRequiredTime() * 1200L);
        int cRedeemed = configurationSection.getInt("redeemed", 0);
        int cPending = configurationSection.getInt("pending", 0);
        boolean cActive = configurationSection.getBoolean("active", true);
        boolean cCountedPrevious = configurationSection.getBoolean("countedPrevious", false);

        return new RewardData(reward, cTimeLeft, cRedeemed, cPending, cActive, cCountedPrevious);
    }

    private Map<String, Object> serialize(PlayerData playerData) {
        Map<String, Object> map = new HashMap<>();

        map.put("lastKnownName", playerData.getPlayer().getName());
        map.put("playtime", playerData.getPlaytime());
        map.put("afkTime", playerData.getAfkTime());
        map.put("rewards", playerData.getRewards().stream().collect(Collectors.toMap(r -> r.getUuid().toString(), this::serialize)));

        return map;
    }

    private Map<String, Object> serialize(RewardData rewardData) {
        Map<String, Object> map = new HashMap<>();

        map.put("timeLeft", rewardData.getTimeLeft());
        map.put("redeemed", rewardData.getRedeemed());
        map.put("pending", rewardData.getPending());
        map.put("active", rewardData.isActive());
        map.put("countedPrevious", rewardData.hasCountedPrevious());

        return map;
    }
}