package com.backtobedrock.rewardslite.domain;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.enumerations.CountPrevious;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import com.backtobedrock.rewardslite.utilities.PlayerUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Reward {
    private final Rewardslite plugin;

    //data
    private final UUID uuid;
    private final String permissionId;
    private final String permission;
    private final String fileName;
    private final long requiredTime;
    private final boolean loop;
    private final boolean countAfk;
    private final CountPrevious countPrevious;
    private final List<String> rewards;
    private final Display display;
    private final Display displayPending;
    private final Display displayClaimed;
    private final Display displayIneligible;
    private final Notification notification;
    private final Notification broadcastNotification;
    private final Notification pendingNotification;
    private final int inventorySlotsNeeded;
    private final boolean usePermission;
    private final boolean manualClaim;
    private final int maximumAmountPending;
    private final int maximumAmountRedeemed;
    private final List<String> disableGainingPlaytimeInWorlds;
    private final List<String> disableRedeemingInWorlds;

    public Reward(String uuid, String fileName, long requiredTime, boolean loop, boolean countAfk, CountPrevious countPrevious, List<String> rewards, Display display, Display displayPending, Display displayClaimed, Display displayIneligible, Notification notification, Notification broadcastNotification, Notification pendingNotification, int inventorySlotsNeeded, boolean usePermission, boolean manualClaim, int maximumAmountPending, int maximumAmountRedeemed, List<String> disableGainingPlaytimeInWorlds, List<String> disableRedeemingInWorlds) {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.uuid = uuid == null ? UUID.randomUUID() : UUID.fromString(uuid);
        this.permissionId = fileName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        this.permission = String.format("%s.reward.%s", this.plugin.getName().toLowerCase(), this.permissionId);
        this.fileName = fileName;
        this.requiredTime = requiredTime;
        this.loop = loop;
        this.countAfk = countAfk;
        this.countPrevious = countPrevious;
        this.rewards = rewards;
        this.display = display;
        this.displayPending = displayPending;
        this.displayClaimed = displayClaimed;
        this.displayIneligible = displayIneligible;
        this.notification = notification;
        this.broadcastNotification = broadcastNotification;
        this.pendingNotification = pendingNotification;
        this.inventorySlotsNeeded = inventorySlotsNeeded;
        this.usePermission = usePermission;
        this.manualClaim = manualClaim;
        this.maximumAmountPending = maximumAmountPending;
        this.maximumAmountRedeemed = !loop ? 1 : maximumAmountRedeemed;
        this.disableGainingPlaytimeInWorlds = disableGainingPlaytimeInWorlds;
        this.disableRedeemingInWorlds = disableRedeemingInWorlds;
    }

    public Reward(Reward reward) {
        this(reward.uuid.toString(), reward.fileName, reward.requiredTime, reward.loop, reward.countAfk, reward.countPrevious, reward.rewards, reward.display, reward.displayPending, reward.displayClaimed, reward.displayIneligible, reward.notification, reward.broadcastNotification, reward.pendingNotification, reward.inventorySlotsNeeded, reward.usePermission, reward.manualClaim, reward.maximumAmountPending, reward.maximumAmountRedeemed, reward.disableGainingPlaytimeInWorlds, reward.disableRedeemingInWorlds);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Display getDisplay() {
        return display;
    }

    public long getRequiredTime() {
        return requiredTime;
    }

    public boolean isLoop() {
        return loop;
    }

    public List<String> getRewards() {
        return rewards;
    }

    public boolean isCountAfk() {
        return countAfk;
    }

    public CountPrevious getCountPrevious() {
        return countPrevious;
    }

    public boolean redeemReward(Player player, boolean manual) {
        if (player.getLocation().getWorld() != null && this.disableRedeemingInWorlds.contains(player.getLocation().getWorld().getName().toLowerCase())) {
            if (manual) {
                player.sendMessage(this.plugin.getMessages().getDisabledWorldRedeemWarning(this.display.getName(), player.getWorld().getName()));
            }
            return false;
        }

        if (this.inventorySlotsNeeded > PlayerUtils.getEmptyInventorySlots(player)) {
            if (manual) {
                player.sendMessage(this.plugin.getMessages().getInventoryFullRedeemWarning(this.display.getName(), Integer.toString(this.inventorySlotsNeeded)));
            }
            return false;
        }

        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("player", player.getName());
            put("player_uuid", player.getUniqueId().toString());
        }};
        this.rewards.forEach(r -> Bukkit.getScheduler().runTask(this.plugin, () -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), replaceAllPlaceholders(player, r, placeholders))));

        this.notify(player);
        return true;
    }

    private String replaceAllPlaceholders(Player player, String text, Map<String, String> internalPlaceholders) {
        String result = MessageUtils.replacePlaceholders(text, internalPlaceholders);
        if (Rewardslite.PAPI_ENABLED) {
            result = PlaceholderAPI.setPlaceholders(player, result);
        }
        return result;
    }

    private void notify(Player player) {
        if (this.notification != null)
            this.notification.getNotifications().forEach(n -> n.notify(player, player.getName()));
        if (this.broadcastNotification != null)
            this.broadcastNotification.getNotifications().forEach(n -> n.notify(new ArrayList<>(this.plugin.getServer().getOnlinePlayers()), player.getName()));
    }

    public boolean isUsePermission() {
        return usePermission;
    }

    public boolean hasPermission(Player player) {
        return !this.isUsePermission() || player.hasPermission(this.permission) || player.hasPermission(String.format("%s.reward.*", this.plugin.getName()));
    }

    public boolean isManualClaim() {
        return manualClaim;
    }

    public String getFileName() {
        return fileName;
    }

    public int getInventorySlotsNeeded() {
        return inventorySlotsNeeded;
    }

    public int getMaximumAmountPending() {
        return maximumAmountPending;
    }

    public List<String> getDisableGainingPlaytimeInWorlds() {
        return disableGainingPlaytimeInWorlds;
    }

    public List<String> getDisableRedeemingInWorlds() {
        return disableRedeemingInWorlds;
    }

    public Notification getNotification() {
        return notification;
    }

    public Notification getBroadcastNotification() {
        return broadcastNotification;
    }

    public Display getDisplayPending() {
        return displayPending;
    }

    public Display getDisplayIneligible() {
        return displayIneligible;
    }

    public Notification getPendingNotification() {
        return pendingNotification;
    }

    public Display getDisplayClaimed() {
        return displayClaimed;
    }

    public String getPermission() {
        return permission;
    }

    public int getMaximumAmountRedeemed() {
        return maximumAmountRedeemed;
    }

    public String getPermissionId() {
        return permissionId;
    }
}
