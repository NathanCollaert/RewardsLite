package com.backtobedrock.rewardslite.utilities;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.enumerations.*;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class ConfigUtils {
    public static int checkMin(String id, int value, int min) {
        if (value >= min) {
            return value;
        } else {
            sendErrorMessage(String.format("%s: value cannot be lower than %d, your value is: %d", id, min, value));
            return -10;
        }
    }

    public static double checkMin(String id, double value, double min) {
        if (value >= min) {
            return value;
        } else {
            sendErrorMessage(String.format("%s: value cannot be lower than %f, your value is: %f", id, min, value));
            return -10;
        }
    }

    public static double checkMinMax(String id, double value, double min, double max) {
        if (value >= min && value <= max) {
            return value;
        } else {
            sendErrorMessage(String.format("%s: value cannot be lower than %f and higher than %f, your value is: %f", id, min, max, value));
            return -10;
        }
    }

    public static int checkMinMax(String id, int value, int min, int max) {
        if (value >= min && value <= max) {
            return value;
        } else {
            sendErrorMessage(String.format("%s: value cannot be lower than %d and higher than %d, your value is: %d", id, min, max, value));
            return -10;
        }
    }

    public static StorageType getStorageType(String id, String storageType) {
        try {
            if (storageType != null) {
                return StorageType.valueOf(storageType.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            sendErrorMessage(String.format("%s: %s is not an existing storage type.", id, storageType));
        }
        return null;
    }

    public static RewardStatus getRewardStatus(String id, String rewardStatus) {
        try {
            if (rewardStatus != null) {
                return RewardStatus.valueOf(rewardStatus.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            sendErrorMessage(String.format("%s: %s is not an existing reward status.", id, rewardStatus));
        }
        return null;
    }

    public static Material getMaterial(String id, String material) {
        try {
            if (material != null) {
                return Material.valueOf(material.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            sendErrorMessage(String.format("%s: %s is not an existing material.", id, material));
        }
        return null;
    }

    public static BarStyle getBarStyle(String id, String barStyle, BarStyle type) {
        try {
            if (barStyle != null) {
                return BarStyle.valueOf(barStyle.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            sendErrorMessage(String.format("%s: %s is not an existing bar type, default value will be used: %s.", id, barStyle, type));
        }
        return type;
    }

    public static BarColor getBarColor(String id, String barColor, BarColor type) {
        try {
            if (barColor != null) {
                return BarColor.valueOf(barColor.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            sendErrorMessage(String.format("%s: %s is not an existing bar color, default value will be used: %s.", id, barColor, type));
        }
        return type;
    }

    public static NotificationType getNotificationType(String id, String notificationType) {
        try {
            if (notificationType != null) {
                return NotificationType.valueOf(notificationType.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            sendErrorMessage(String.format("%s: %s is not an existing notification type.", id, notificationType));
        }
        return null;
    }

    public static CountPrevious getCountPrevious(String id, String countPrevious) {
        try {
            if (countPrevious != null) {
                return CountPrevious.valueOf(countPrevious.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            sendErrorMessage(String.format("%s: %s is not an existing count previous option.", id, countPrevious));
        }
        return null;
    }

    public static TimeUnit getTimeUnit(String id, String timeUnit) {
        try {
            if (timeUnit != null) {
                return TimeUnit.valueOf(timeUnit.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            sendErrorMessage(String.format("%s: %s is not an existing time unit option.", id, timeUnit));
        }
        return null;
    }

    private static void sendErrorMessage(String message) {
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);
        plugin.getLogger().log(Level.SEVERE, message);
    }

    private static void sendWarningMessage(String message) {
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);
        plugin.getLogger().log(Level.INFO, message);
    }
}
