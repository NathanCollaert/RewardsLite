package com.backtobedrock.LitePlaytimeRewards.helperClasses;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;

public class ConfigReward {

    private final String displayName;
    private final Material displayItem;
    private final List<String> displayDescription;
    private final List<Long> playtimeNeeded;
    private final boolean countAfkTime;
    private final int slotsNeeded;
    private final boolean loop;
    private final List<String> disabledWorlds;
    private final boolean UsePermission;
    private final String notificationType;
    private final String notification;
    private final String broadcastNotification;
    private final List<String> commands;

    public ConfigReward(String displayName, Material displayItem, List<String> displayDescription, List<Long> playtimeNeeded, boolean countAfkTime, int slotsNeeded, boolean loop, List<String> disabledWorlds, boolean UsePermission, String notificationType, String notification, String broadcastNotification, List<String> commands) {
        this.displayName = displayName;
        this.displayItem = displayItem;
        this.displayDescription = displayDescription;
        this.playtimeNeeded = playtimeNeeded;
        this.countAfkTime = countAfkTime;
        this.slotsNeeded = slotsNeeded;
        this.loop = loop;
        this.disabledWorlds = disabledWorlds;
        this.UsePermission = UsePermission;
        this.notificationType = notificationType;
        this.notification = notification;
        this.broadcastNotification = broadcastNotification;
        this.commands = commands;
    }

    public ConfigReward(ConfigReward config) {
        this(config.displayName, config.displayItem, config.displayDescription, config.playtimeNeeded, config.countAfkTime, config.slotsNeeded, config.loop, config.disabledWorlds, config.UsePermission, config.notificationType, config.notification, config.broadcastNotification, config.commands);
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Long> getPlaytimeNeeded() {
        return playtimeNeeded;
    }

    public boolean isCountAfkTime() {
        return countAfkTime;
    }

    public boolean isLoop() {
        return loop;
    }

    public List<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    public String getNotification() {
        return notification.replaceAll("&", "§");
    }

    public String getBroadcastNotification() {
        return broadcastNotification.replaceAll("&", "§");
    }

    public List<String> getCommands() {
        return commands;
    }

    public int getSlotsNeeded() {
        return slotsNeeded;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public boolean isUsePermission() {
        return UsePermission;
    }

    public Material getDisplayItem() {
        return displayItem;
    }

    public List<String> getDisplayDescription() {
        return displayDescription.stream().map(e -> e.replaceAll("&", "§")).collect(Collectors.toList());
    }
}
