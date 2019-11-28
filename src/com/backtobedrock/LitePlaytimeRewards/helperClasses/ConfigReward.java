package com.backtobedrock.LitePlaytimeRewards.helperClasses;

import java.util.List;

public class ConfigReward {

    private final String displayName;
    private final int playtimeNeeded;
    private final boolean countPlaytimeFromStart;
    private final int slotsNeeded;
    private final boolean loop;
    private final String notification;
    private final String broadcastNotification;
    private final List<String> commands;

    public ConfigReward(String displayName, int playtimeNeeded, boolean countPlaytimeFromStart, int slotsNeeded, boolean loop, String notification, String broadcastNotification, List<String> commands) {
        this.displayName = displayName;
        this.playtimeNeeded = playtimeNeeded;
        this.countPlaytimeFromStart = countPlaytimeFromStart;
        this.slotsNeeded = slotsNeeded;
        this.loop = loop;
        this.notification = notification;
        this.broadcastNotification = broadcastNotification;
        this.commands = commands;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPlaytimeNeeded() {
        return playtimeNeeded;
    }

    public boolean isLoop() {
        return loop;
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

    public boolean isCountPlaytimeFromStart() {
        return countPlaytimeFromStart;
    }

    public int getSlotsNeeded() {
        return slotsNeeded;
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
