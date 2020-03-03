package com.backtobedrock.LitePlaytimeRewards.helperClasses;

import java.util.ArrayList;
import java.util.List;

public class ConfigReward {

    private final String displayName;
    private final List<Long> playtimeNeeded;
    private final boolean countAfkTime;
    private final int slotsNeeded;
    private final boolean loop;
    private final List<String> disabledWorlds;
    private final String notificationType;
    private final String notification;
    private final String broadcastNotification;
    private final List<String> commands;

    public ConfigReward(String displayName, List<Long> playtimeNeeded, boolean countAfkTime, int slotsNeeded, boolean loop, List<String> disabledWorlds, String notificationType, String notification, String broadcastNotification, List<String> commands) {
        this.displayName = displayName;
        this.playtimeNeeded = playtimeNeeded;
        this.countAfkTime = countAfkTime;
        this.slotsNeeded = slotsNeeded;
        this.loop = loop;
        this.disabledWorlds = disabledWorlds;
        this.notificationType = notificationType;
        this.notification = notification;
        this.broadcastNotification = broadcastNotification;
        this.commands = commands;
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

//    @Override
//    public Map<String, Object> serialize() {
//        Map<String, Object> map = new TreeMap<>();
//
//        map.put("DisplayName", this.displayName);
//        map.put("PlaytimeNeeded", this.playtimeNeeded);
//        map.put("CountPlaytimeFromStart", this.countPlaytimeFromStart);
//        map.put("Loop", this.loop);
//        map.put("Notification", this.notification);
//        map.put("BroadcastNotification", this.broadcastNotification);
//        map.put("Commands", this.commands);
//
//        return map;
//    }
}
