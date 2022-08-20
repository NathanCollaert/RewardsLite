package com.backtobedrock.rewardslite.mappers.reward;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.Display;
import com.backtobedrock.rewardslite.domain.Notification;
import com.backtobedrock.rewardslite.domain.Reward;
import com.backtobedrock.rewardslite.domain.enumerations.CountPrevious;
import com.backtobedrock.rewardslite.mappers.AbstractMapper;
import com.backtobedrock.rewardslite.utilities.ConfigUtils;
import com.tchristofferson.configupdater.ConfigUpdater;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YAMLRewardMapper extends AbstractMapper implements IRewardMapper {
    private final Rewardslite plugin;

    public YAMLRewardMapper() {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
    }

    @Override
    public List<Reward> getAll() {
        List<Reward> rewards = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(this.getDirectory().toPath())) {
            for (Path path : walk.filter(Files::isRegularFile).collect(Collectors.toList())) {
                Reward reward = this.getPlaytimeRewardFromFile(path.toFile());
                if (reward != null) {
                    Reward duplicate = rewards.stream().filter(r -> r.getUuid().equals(reward.getUuid())).findFirst().orElse(null);
                    if (duplicate == null) {
                        rewards.add(reward);
                    } else {
                        this.plugin.getLogger().log(Level.SEVERE, String.format("Found duplicate UUID on rewards %s.yml and %s.yml. Please make sure you have removed the generated section from all newly created rewards. %s has been disabled to prevent player data corruption and progress loss.", reward.getFileName(), duplicate.getFileName(), this.plugin.getName()));
                        this.plugin.getPluginLoader().disablePlugin(this.plugin);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, e.getMessage());
        }
        return rewards;
    }

    @Override
    public void upsertReward(Reward reward) {
        File file = getFile(reward);
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        this.serialize(reward).forEach(configuration::set);
        try {
            configuration.save(file);
            ConfigUpdater.update(this.plugin, "reward-template.yml", file, Collections.emptyList());
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Cannot upsert to {0}", file.getName());
        }
    }

    private Reward getPlaytimeRewardFromFile(File playtimeRewardFile) {
        FileConfiguration playtimeReward = YamlConfiguration.loadConfiguration(playtimeRewardFile);
        int pos = playtimeRewardFile.getName().lastIndexOf(".");
        if (pos > 0) {
            String id = playtimeRewardFile.getName().substring(0, pos);
            return this.deserialize(id, playtimeReward);
        } else {
            this.plugin.getLogger().log(Level.SEVERE, "File name cannot be empty for a playtime reward.");
        }
        return null;
    }

    private Reward deserialize(String fileName, FileConfiguration section) {
        try {
            String cUuid = section.getString("generated.uuid");
            long cRequiredTime = ConfigUtils.checkMinMax(fileName + ".requiredTime", section.getInt("requiredTime"), 1, Integer.MAX_VALUE);
            boolean cLoop = section.getBoolean("loop", false);
            boolean cCountAfk = section.getBoolean("countAfk", false);
            CountPrevious cCountPrevious = ConfigUtils.getCountPrevious(fileName + ".countPrevious", section.getString("countPrevious", "NONE"));
            List<String> cRewards = section.getStringList("rewards");
            Display cDisplay;
            Display cDisplayPending;
            Display cDisplayClaimed;
            Display cDisplayIneligible;
            Notification cNotification = null;
            Notification cBroadcastNotification = null;
            Notification cPendingNotification = null;
            int cInventorySlotsNeeded = ConfigUtils.checkMinMax(fileName + ".inventorySlotsNeeded", section.getInt("inventorySlotsNeeded", 0), 0, Integer.MAX_VALUE);
            boolean cUsePermission = section.getBoolean("usePermission", false);
            boolean cManualClaim = section.getBoolean("manualClaim", false);
            int cMaximumAmountPending = ConfigUtils.checkMinMax(fileName + ".maximumAmountPending", section.getInt("maximumAmountPending", Integer.MAX_VALUE), -1, Integer.MAX_VALUE);
            int cMaximumAmountRedeemed = ConfigUtils.checkMinMax(fileName + ".maximumAmountRedeemed", section.getInt("maximumAmountRedeemed", Integer.MAX_VALUE), -1, Integer.MAX_VALUE);
            List<String> cDisableGainingPlaytimeInWorlds = section.getStringList("disableGainingPlaytimeInWorlds").stream().map(String::toLowerCase).collect(Collectors.toList());
            List<String> cDisableRedeemingInWorlds = section.getStringList("disableRedeemingInWorlds").stream().map(String::toLowerCase).collect(Collectors.toList());

            ConfigurationSection displaySection = section.getConfigurationSection("display");
            cDisplay = displaySection != null ? Display.deserialize(fileName + ".display", displaySection) : null;
            ConfigurationSection displayPendingSection = section.getConfigurationSection("displayPending");
            cDisplayPending = displayPendingSection != null ? Display.deserialize(fileName + ".displayPending", displayPendingSection) : cDisplay;
            ConfigurationSection displayClaimedSection = section.getConfigurationSection("displayClaimed");
            cDisplayClaimed = displayClaimedSection != null ? Display.deserialize(fileName + ".displayClaimed", displayClaimedSection) : cDisplay;
            ConfigurationSection displayIneligibleSection = section.getConfigurationSection("displayIneligible");
            cDisplayIneligible = displayIneligibleSection != null ? Display.deserialize(fileName + ".displayIneligible", displayIneligibleSection) : cDisplay;

            ConfigurationSection notificationSection = section.getConfigurationSection("notification");
            if (notificationSection != null) {
                cNotification = Notification.deserialize(fileName + ".notification", notificationSection);
            }
            ConfigurationSection broadcastNotificationSection = section.getConfigurationSection("broadcastNotification");
            if (broadcastNotificationSection != null) {
                cBroadcastNotification = Notification.deserialize(fileName + ".broadcastNotification", broadcastNotificationSection);
            }
            ConfigurationSection pendingNotificationSection = section.getConfigurationSection("pendingNotification");
            if (pendingNotificationSection != null) {
                cPendingNotification = Notification.deserialize(fileName + ".pendingNotification", pendingNotificationSection);
            }

            if (cMaximumAmountPending == -1) {
                cMaximumAmountPending = Integer.MAX_VALUE;
            }
            if (cMaximumAmountRedeemed == -1) {
                cMaximumAmountRedeemed = Integer.MAX_VALUE;
            } else if (!cLoop) {
                cMaximumAmountRedeemed = 1;
            }

            if (cRequiredTime == -10 || cDisplay == null || cInventorySlotsNeeded == -10 || cMaximumAmountPending == -10 || cMaximumAmountRedeemed == -10) {
                return null;
            }

            Reward reward = new Reward(cUuid, fileName, cRequiredTime, cLoop, cCountAfk, cCountPrevious, cRewards, cDisplay, cDisplayPending, cDisplayClaimed, cDisplayIneligible, cNotification, cBroadcastNotification, cPendingNotification, cInventorySlotsNeeded, cUsePermission, cManualClaim, cMaximumAmountPending, cMaximumAmountRedeemed, cDisableGainingPlaytimeInWorlds, cDisableRedeemingInWorlds);
            this.upsertReward(reward);
            return reward;
        } catch (Exception e) {
            this.plugin.getLogger().log(Level.SEVERE, e.getMessage());
        }
        return null;
    }

    private Map<String, Object> serialize(Reward reward) {
        HashMap<String, Object> object = new HashMap<>();
        object.put("generated.uuid", reward.getUuid().toString());
        object.put("generated.permission", reward.getPermission());
        object.put("requiredTime", reward.getRequiredTime());
        object.put("loop", reward.isLoop());
        object.put("countAfk", reward.isCountAfk());
        object.put("countPrevious", reward.getCountPrevious().toString());
        object.put("rewards", reward.getRewards());
        object.put("display", reward.getDisplay().serialize());
        object.put("displayPending", reward.getDisplayPending().serialize());
        object.put("displayClaimed", reward.getDisplayClaimed().serialize());
        object.put("displayIneligible", reward.getDisplayIneligible().serialize());
        object.put("notification", reward.getNotification() == null ? null : reward.getNotification().serialize());
        object.put("broadcastNotification", reward.getBroadcastNotification() == null ? null : reward.getBroadcastNotification().serialize());
        object.put("pendingNotification", reward.getPendingNotification() == null ? null : reward.getPendingNotification().serialize());
        object.put("inventorySlotsNeeded", reward.getInventorySlotsNeeded());
        object.put("usePermission", reward.isUsePermission());
        object.put("manualClaim", reward.isManualClaim());
        object.put("maximumAmountPending", reward.getMaximumAmountPending() == Integer.MAX_VALUE ? -1 : reward.getMaximumAmountPending());
        object.put("maximumAmountRedeemed", reward.getMaximumAmountRedeemed() == Integer.MAX_VALUE ? -1 : reward.getMaximumAmountRedeemed());
        object.put("disableGainingPlaytimeInWorlds", reward.getDisableGainingPlaytimeInWorlds());
        object.put("disableRedeemingInWorlds", reward.getDisableRedeemingInWorlds());
        return object;
    }

    private File getDirectory() {
        File rewardsDirectory = new File(this.plugin.getDataFolder(), "rewards");
        if (!rewardsDirectory.exists() && rewardsDirectory.mkdir()) {
            //initialize default reward
            File defaultResource = new File(this.plugin.getDataFolder(), "/rewards/default-reward.yml");
            try {
                if (defaultResource.createNewFile()) {
                    this.plugin.getLogger().log(Level.INFO, "Creating {0}.", defaultResource.getAbsolutePath());
                    InputStream initialStream = this.plugin.getResource("default-reward.yml");
                    if (initialStream != null) {
                        Files.copy(initialStream, defaultResource.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE, e.getMessage());
            }
        }
        return rewardsDirectory;
    }

    private File getFile(Reward reward) {
        File file = new File(this.plugin.getDataFolder(), String.format("/rewards/%s.yml", reward.getFileName()));
        try {
            if (file.createNewFile()) {
                this.plugin.getLogger().log(Level.INFO, "Creating {0}.", file.getAbsolutePath());
            }
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE, e.getMessage());
        }
        return file;
    }
}
