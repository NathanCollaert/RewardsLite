package com.backtobedrock.LitePlaytimeRewards.helperClasses;

import java.util.List;

public class Reward {

    private final String displayName;
    private final int playtimeNeeded;
    private final boolean loop;
    private final String notification;
    private final String broadcastNotification;
    private final List<String> commands;

    public Reward(String displayName, int playtimeNeeded, boolean loop, String notification, String broadcastNotification, List<String> commands) {
        this.displayName = displayName;
        this.playtimeNeeded = playtimeNeeded;
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

    @Override
    public String toString() {
        return "Reward{" + "displayName=" + displayName + ", playtimeNeeded=" + playtimeNeeded + ", loop=" + loop + ", notification=" + notification + ", broadcastNotification=" + broadcastNotification + ", commands=" + commands + '}';
    }
}
