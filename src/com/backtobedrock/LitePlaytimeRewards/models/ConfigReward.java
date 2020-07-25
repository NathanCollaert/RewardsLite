package com.backtobedrock.LitePlaytimeRewards.models;

import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Material;

public class ConfigReward {

    private final String id;
    private final String displayName;
    private final Material displayItem;
    private final List<String> displayDescription;
    private final List<Integer> playtimeNeeded;
    private final boolean countAfkTime;
    private final int slotsNeeded;
    private final boolean loop;
    private final List<String> disabledWorlds;
    private final boolean UsePermission;
    private final String notificationType;
    private final String notification;
    private final String broadcastNotification;
    private final List<String> commands;

    public ConfigReward(String id, String displayName, Material displayItem, List<String> displayDescription, List<Integer> playtimeNeeded, boolean countAfkTime, int slotsNeeded, boolean loop, List<String> disabledWorlds, boolean UsePermission, String notificationType, String notification, String broadcastNotification, List<String> commands) {
        this.id = id;
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
        this(config.id, config.displayName, config.displayItem, config.displayDescription, config.playtimeNeeded, config.countAfkTime, config.slotsNeeded, config.loop, config.disabledWorlds, config.UsePermission, config.notificationType, config.notification, config.broadcastNotification, config.commands);
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Integer> getPlaytimeNeeded() {
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
        return notification;
    }

    public String getBroadcastNotification() {
        return broadcastNotification;
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
        return displayDescription;
    }

    public String getId() {
        return id;
    }

    public List<String> getGiveRewardGUIDescription(GUIReward reward) {
        List<String> description = (this.getDisplayDescription().stream().collect(Collectors.toList()));

        if (description.size() > 0) {
            description.add(0, "§f----");
        }

        description.add(0, String.format("§a(amount: §2%d§a, broadcast: §2%s§a)", reward.getAmount(), reward.isBroadcast()));
        description.add("§f----");
        description.add("§6§oRight click §e§oto toggle broadcasting");
        description.add("§6§oShift left click §e§oto increase amount");
        description.add("§6§oShift right click §e§oto decrease amount");

        return description;
    }
}
