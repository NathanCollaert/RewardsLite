package com.backtobedrock.LitePlaytimeRewards.models;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.enums.NotificationType;
import com.backtobedrock.LitePlaytimeRewards.utils.ConfigUtils;
import com.backtobedrock.LitePlaytimeRewards.utils.HexUtils;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

public class ConfigReward {

    private final LitePlaytimeRewards plugin;

    private final String id;
    private final String displayName;
    private final Material displayItem;
    private final List<String> displayDescription;
    private final List<Integer> playtimeNeeded;
    private final boolean countAfkTime;
    private final boolean countAllPlaytime;
    private final int slotsNeeded;
    private final boolean loop;
    private final List<String> disabledWorlds;
    private final boolean usePermission;
    private final NotificationType notificationType;
    private final String notification;
    private final String broadcastNotification;
    private final List<String> commands;

    public ConfigReward(String id, String displayName, Material displayItem, List<String> displayDescription, List<Integer> playtimeNeeded, boolean countAfkTime, boolean countAllPlaytime, int slotsNeeded, boolean loop, List<String> disabledWorlds, boolean usePermission, NotificationType notificationType, String notification, String broadcastNotification, List<String> commands) {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        this.id = id;
        this.displayName = displayName;
        this.displayItem = displayItem;
        this.displayDescription = displayDescription;
        this.playtimeNeeded = playtimeNeeded;
        this.countAfkTime = countAfkTime;
        this.countAllPlaytime = countAllPlaytime;
        this.slotsNeeded = slotsNeeded;
        this.loop = loop;
        this.disabledWorlds = disabledWorlds;
        this.usePermission = usePermission;
        this.notificationType = notificationType;
        this.notification = notification;
        this.broadcastNotification = broadcastNotification;
        this.commands = commands;
    }

    public ConfigReward(ConfigReward config) {
        this(config.id, config.displayName, config.displayItem, config.displayDescription, config.playtimeNeeded, config.countAfkTime, config.countAllPlaytime, config.slotsNeeded, config.loop, config.disabledWorlds, config.usePermission, config.notificationType, config.notification, config.broadcastNotification, config.commands);
    }

    public static ConfigReward deserialize(String id, ConfigurationSection reward) {
        LitePlaytimeRewards plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);

        String displayName = reward.getString("DisplayName", id);
        Material displayItem = Material.CHEST;
        List<String> displayDescription = reward.getStringList("DisplayDescription");
        List<Integer> playtimeNeeded = ConfigUtils.getNumbersFromString(reward.getString("PlaytimeNeeded", ""));
        boolean countAfkTime = reward.getBoolean("CountAfkTime", true);
        boolean countAllPlaytime = reward.getBoolean("CountAllPlaytime", false);
        int slotsNeeded = ConfigUtils.checkMin(reward.getInt("SlotsNeeded", 0), 0, 0);
        boolean loop = reward.getBoolean("Loop", false);
        List<String> disabledWorlds = reward.getStringList("DisabledWorlds");
        boolean usePermission = reward.getBoolean("UsePermission", false);
        NotificationType notificationType = ConfigUtils.getNotificationType("NotificationType", reward.getString("NotificationType", "bossbar"), NotificationType.BOSSBAR);
        String notification = HexUtils.applyColor(reward.getString("Notification", ""));
        String broadcastNotification = HexUtils.applyColor(reward.getString("BroadcastNotification", ""));
        List<String> commands = reward.getStringList("Commands");

        Material m = Material.matchMaterial(reward.getString("DisplayItem", "chest"));
        if (m != null && m != Material.AIR && m.isItem()) {
            displayItem = m;
        } else {
            plugin.getLogger().log(Level.WARNING, "{0}: {1} is not an item in game. Default DisplayItem used.", new Object[]{id, m.name()});
        }

        if (playtimeNeeded.isEmpty()) {
            plugin.getLogger().log(Level.SEVERE, "{0}: PlaytimeNeeded can not be empty. Reward was not loaded.", id);
            return null;
        } else if (slotsNeeded < 0) {
            plugin.getLogger().log(Level.SEVERE, "{0}: slotsNeeded can not be lower then 0. Reward was not loaded.", id);
            return null;
        } else if (commands.isEmpty()) {
            plugin.getLogger().log(Level.SEVERE, "{0}: Commands can not be empty. Reward was not loaded.", id);
            return null;
        } else {
            return new ConfigReward(id.toLowerCase(), displayName, displayItem, displayDescription, playtimeNeeded, countAfkTime, countAllPlaytime, slotsNeeded, loop, disabledWorlds, usePermission, notificationType, notification.replaceAll("&", "§"), broadcastNotification.replaceAll("&", "§"), commands);
        }
    }

    public String getDisplayName() {
        return HexUtils.applyColor(displayName);
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

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public boolean isUsePermission() {
        return usePermission;
    }

    public Material getDisplayItem() {
        return displayItem;
    }

    public List<String> getDisplayDescription() {
        return HexUtils.applyColor(displayDescription);
    }

    public String getId() {
        return id;
    }

    public boolean isCountAllPlaytime() {
        return countAllPlaytime;
    }

    public List<String> getGiveRewardGUIDescription(GUIReward reward) {
        List<String> description = this.getDisplayDescription();

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

    public Map<String, Object> serialize() {
        Map<String, Object> map = new TreeMap<>();
        StringBuilder sb = new StringBuilder();
        this.playtimeNeeded.forEach(e -> sb.append(e / 1200).append(","));
        sb.deleteCharAt(sb.length() - 1);

        map.put("DisplayName", this.displayName);
        map.put("DisplayItem", this.displayItem.toString());
        map.put("DisplayDescription", this.displayDescription);
        map.put("PlaytimeNeeded", sb.toString());
        map.put("CountAfkTime", this.countAfkTime);
        map.put("CountAllPlaytime", this.countAllPlaytime);
        map.put("SlotsNeeded", this.slotsNeeded);
        map.put("Loop", this.loop);
        map.put("DisabledWorlds", this.disabledWorlds);
        map.put("UsePermission", this.usePermission);
        map.put("NotificationType", this.notificationType.toString());
        map.put("Notification", this.notification);
        map.put("BroadcastNotification", this.broadcastNotification);
        map.put("Commands", this.commands);

        return map;
    }

    @Override
    public String toString() {
        return "ConfigReward{" + "displayName=" + displayName + ", displayItem=" + displayItem + ", displayDescription=" + displayDescription + ", playtimeNeeded=" + playtimeNeeded + ", countAfkTime=" + countAfkTime + ", countAllPlaytime=" + countAllPlaytime + ", slotsNeeded=" + slotsNeeded + ", loop=" + loop + ", disabledWorlds=" + disabledWorlds + ", usePermission=" + usePermission + ", notificationType=" + notificationType + ", notification=" + notification + ", broadcastNotification=" + broadcastNotification + ", commands=" + commands + '}';
    }
}
