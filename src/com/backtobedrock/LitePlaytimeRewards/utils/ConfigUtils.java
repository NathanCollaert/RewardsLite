package com.backtobedrock.LitePlaytimeRewards.utils;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.enums.InventoryLayout;
import com.backtobedrock.LitePlaytimeRewards.enums.NotificationType;
import com.backtobedrock.LitePlaytimeRewards.enums.RewardsOrder;
import com.backtobedrock.LitePlaytimeRewards.models.ConfigReward;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ConfigUtils {

    public static List<Integer> getNumbersFromString(String numbers) {
        try {
            return Arrays.stream(numbers.split(",")).map(e -> (Math.min(Integer.parseInt(e), 1789569)) * 1200).collect(Collectors.toList());
        } catch (NumberFormatException e) {
            return new ArrayList<>();
        }
    }

    public static int checkMin(int value, int min, int defaultValue) {
        if (value >= min) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public static Material getMaterial(String id, String configMaterial, Material material) {
        Material mat = Material.matchMaterial(configMaterial);
        if (mat == null) {
            JavaPlugin.getPlugin(LitePlaytimeRewards.class).getLogger().log(Level.SEVERE, "{0} was not an existing material for {1}.", new Object[]{configMaterial, id});
            return material;
        }
        return mat;
    }

    public static void convertOldRewards() {
        LitePlaytimeRewards plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);

        TreeMap<String, ConfigReward> rewards = plugin.getLPRConfig().getRewards();
        if (!rewards.isEmpty()) {
            rewards.forEach((key, value) -> plugin.getRewards().getAll().putIfAbsent(key, value));
            plugin.getRewards().saveRewards();
            JavaPlugin.getPlugin(LitePlaytimeRewards.class).getLogger().log(Level.SEVERE, "Found rewards in config.yml, moved to rewards.yml.");
        }

        plugin.getLogger().log(Level.INFO, "Loaded {0} rewards.", plugin.getRewards().getAll().size());
    }

    public static InventoryLayout getInventoryLayout(String id, String configLayout, InventoryLayout layout) {
        InventoryLayout clayout = Arrays.stream(InventoryLayout.values()).filter(e -> e.name().equals(configLayout.toUpperCase())).findFirst().get();
        if (clayout == null) {
            JavaPlugin.getPlugin(LitePlaytimeRewards.class).getLogger().log(Level.SEVERE, "{0} was not an inventory layout for {1}.", new Object[]{configLayout, id});
            return layout;
        }
        return clayout;
    }

    public static RewardsOrder getRewardsOrder(String id, String configOrder, RewardsOrder order) {
        RewardsOrder corder = Arrays.stream(RewardsOrder.values()).filter(e -> e.name().equals(configOrder.toUpperCase())).findFirst().get();
        if (corder == null) {
            JavaPlugin.getPlugin(LitePlaytimeRewards.class).getLogger().log(Level.SEVERE, "{0} was not an inventory layout for {1}.", new Object[]{configOrder, id});
            return order;
        }
        return corder;
    }

    public static NotificationType getNotificationType(String id, String configNotification, NotificationType notification) {
        NotificationType cnotification = Arrays.stream(NotificationType.values()).filter(e -> e.name().equals(configNotification.toUpperCase())).findFirst().get();
        if (cnotification == null) {
            JavaPlugin.getPlugin(LitePlaytimeRewards.class).getLogger().log(Level.SEVERE, "{0} was not an inventory layout for {1}.", new Object[]{configNotification, id});
            return notification;
        }
        return cnotification;
    }
}
