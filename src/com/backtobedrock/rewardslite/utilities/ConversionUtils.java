package com.backtobedrock.rewardslite.utilities;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.Display;
import com.backtobedrock.rewardslite.domain.Notification;
import com.backtobedrock.rewardslite.domain.Reward;
import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.domain.data.PlayerData;
import com.backtobedrock.rewardslite.domain.enumerations.CountPrevious;
import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import com.backtobedrock.rewardslite.domain.enumerations.StorageType;
import com.backtobedrock.rewardslite.domain.notifications.ActionBarNotification;
import com.backtobedrock.rewardslite.domain.notifications.BossBarNotification;
import com.backtobedrock.rewardslite.domain.notifications.ChatNotification;
import com.backtobedrock.rewardslite.mappers.player.MySQLPlayerMapper;
import com.backtobedrock.rewardslite.mappers.player.YAMLPlayerMapper;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConversionUtils {
    public static void convertLitePlaytimeRewardsRewards(CommandSender commandSender) {
        commandSender.sendMessage("§7Starting rewards conversion...");
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);
        File lprRewardsFile = new File(plugin.getDataFolder().getParentFile(), "LitePlaytimeRewards/rewards.yml");
        if (!lprRewardsFile.exists()) {
            commandSender.sendMessage(String.format("§c%s was not found.", plugin.getDataFolder().getParentFile() + "LitePlaytimeRewards/rewards.yml"));
        } else {
            ConfigurationSection rewardsSection = YamlConfiguration.loadConfiguration(lprRewardsFile).getConfigurationSection("Rewards");
            if (rewardsSection != null) {
                rewardsSection.getKeys(false).forEach(e -> {
                    ConfigurationSection rewardConfig = rewardsSection.getConfigurationSection(e);
                    if (rewardConfig != null) {
                        Reward reward = convertLitePlaytimeRewardsReward(e, rewardConfig, commandSender);
                        if (reward != null) {
                            plugin.getRewardsRepository().updateReward(reward);
                        }
                    }
                });
                plugin.getRewardsRepository().clearRewardsCache();
            }
        }
    }

    public static void convertLitePlaytimeRewardsPlayerData(CommandSender commandSender) {
        commandSender.sendMessage("§7Starting player data conversion...");
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);
        try (Stream<Path> walk = Files.walk(new File(plugin.getDataFolder().getParentFile(), "LitePlaytimeRewards/userdata").toPath())) {
            List<Path> result = walk.filter(Files::isRegularFile).collect(Collectors.toList());
            result.forEach(e -> {
                File file = e.toFile();
                int pos = file.getName().lastIndexOf(".");
                if (pos > 0) {
                    String id = file.getName().substring(0, pos).toLowerCase();
                    try {
                        Files.write(file.toPath(), Files.lines(file.toPath()).map(line -> {
                            if (line.contains("==: Reward")) {
                                return "";
                            } else {
                                return line;
                            }
                        }).collect(Collectors.toList()));
                        PlayerData playerData = convertLitePlaytimeRewardsPlayerData(UUID.fromString(id), YamlConfiguration.loadConfiguration(file));
                        if (plugin.getPlayerRepository().doesCacheContainPlayer(playerData.getPlayer().getUniqueId())) {
                            plugin.getPlayerRepository().setInPlayerCache(playerData);
                        }
                        plugin.getPlayerRepository().updatePlayerData(playerData);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            plugin.getServer().getOnlinePlayers().forEach(p -> plugin.getPlayerRepository().getByPlayerSync(p));
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, e.getMessage());
        }
    }

    public static void convertYamlToMysqlPlayerData(CommandSender commandSender, boolean convertedPlayerData) {
        commandSender.sendMessage("§7Starting YAML to MySQL conversion...");
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);
        if (convertedPlayerData && plugin.getConfigurations().getDataConfiguration().getStorageType() == StorageType.MYSQL) {
            return;
        }

        List<PlayerData> playerData;
        playerData = new YAMLPlayerMapper().getAllSync();
        playerData.forEach(p -> {
            if (plugin.getPlayerRepository().doesCacheContainPlayer(p.getPlayer().getUniqueId())) {
                plugin.getPlayerRepository().setInPlayerCache(p);
            }
            MySQLPlayerMapper.getInstance().updatePlayerData(p);
        });
    }

    private static Reward convertLitePlaytimeRewardsReward(String id, ConfigurationSection section, CommandSender commandSender) {
        String displayName = section.getString("DisplayName", id);
        Material displayItem = Material.CHEST;
        List<String> displayDescription = section.getStringList("DisplayDescription");
        int playtimeNeeded = -10;
        boolean countAfkTime = section.getBoolean("CountAfkTime", true);
        boolean countAllPlaytime = section.getBoolean("CountAllPlaytime", false);
        int slotsNeeded = section.getInt("SlotsNeeded", 0);
        boolean loop = section.getBoolean("Loop", false);
        List<String> disabledWorlds = section.getStringList("DisabledWorlds");
        boolean usePermission = section.getBoolean("UsePermission", false);
        String notificationType = section.getString("NotificationType", "bossbar");
        String notification = section.getString("Notification", "");
        String broadcastNotification = section.getString("BroadcastNotification", "");
        List<String> commands = section.getStringList("Commands");

        Material m = Material.matchMaterial(section.getString("DisplayItem", "chest"));
        if (m != null && m != Material.AIR) {
            displayItem = m;
        }

        try {
            playtimeNeeded = Integer.parseInt(section.getString("PlaytimeNeeded", "-10"));
        } catch (NumberFormatException e) {
            //ignore
        }

        if (playtimeNeeded == -10) {
            commandSender.sendMessage(String.format("§e%s: no playtime was found or multiple times were configured which is no longer supported by RewardsLite.", id));
            return null;
        }

        Display display = new Display(displayItem, false, displayName, new ArrayList<>(displayDescription), 1);
        displayDescription.add(0, "");
        displayDescription.add(0, "&eRedeemed: &6%redeemed% &f| &ePending: &6%pending%");
        displayDescription.add("");
        displayDescription.add("&eTime till next reward");
        displayDescription.add("&6%time_left%");
        Display displayElse = new Display(displayItem, false, displayName, displayDescription, 1);
        Notification notificationUser = null;
        Notification notificationBroadcast = null;

        switch (notificationType.toLowerCase()) {
            case "bossbar":
                MinecraftVersion minecraftVersion = MinecraftVersion.get();
                if (minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_9)) {
                    notificationUser = notification.isEmpty() ? null : new Notification(null, new BossBarNotification(true, notification, BarColor.BLUE, BarStyle.SOLID), null, null);
                    notificationBroadcast = broadcastNotification.isEmpty() ? null : new Notification(null, new BossBarNotification(true, broadcastNotification, BarColor.BLUE, BarStyle.SOLID), null, null);
                }
                break;
            case "chat":
                notificationUser = notification.isEmpty() ? null : new Notification(null, null, new ChatNotification(true, notification), null);
                notificationBroadcast = broadcastNotification.isEmpty() ? null : new Notification(null, null, new ChatNotification(true, broadcastNotification), null);
                break;
            case "actionbar":
                notificationUser = notification.isEmpty() ? null : new Notification(new ActionBarNotification(true, notification), null, null, null);
                notificationBroadcast = broadcastNotification.isEmpty() ? null : new Notification(new ActionBarNotification(true, broadcastNotification), null, null, null);
                break;
        }

        return new Reward(null, id, playtimeNeeded, loop, countAfkTime, countAllPlaytime ? CountPrevious.ALL : CountPrevious.NONE, commands, displayElse, display, display, display, notificationUser, notificationBroadcast, null, slotsNeeded, usePermission, false, Integer.MAX_VALUE, Integer.MAX_VALUE, disabledWorlds, disabledWorlds);
    }

    private static PlayerData convertLitePlaytimeRewardsPlayerData(UUID uuid, ConfigurationSection section) {
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);
        List<RewardData> rewardData = new ArrayList<>();
        ConfigurationSection rewardsSection = section.getConfigurationSection("rewards");
        if (rewardsSection != null) {
            rewardsSection.getKeys(false).forEach(e -> {
                ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(e);
                if (rewardSection != null) {
                    RewardData reward = convertLitePlaytimeRewardsRewardData(e, rewardSection);
                    if (reward != null) {
                        rewardData.add(reward);
                    }
                }
            });
        }
        return new PlayerData(plugin.getServer().getOfflinePlayer(uuid), section.getLong("playtime", 0), section.getLong("afktime", 0), rewardData);
    }

    private static RewardData convertLitePlaytimeRewardsRewardData(String id, ConfigurationSection section) {
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);
        Reward reward = plugin.getRewardsRepository().getByFileName(id.toLowerCase());
        if (reward == null) {
            return null;
        }
        return new RewardData(reward, section.getLongList("timeTillNextReward").get(0), section.getInt("amountRedeemed", 0), section.getInt("amountPending", 0), section.getBoolean("eligible", true), section.getBoolean("claimedOldPlaytime", false));
    }
}
