package com.backtobedrock.rewardslite.domain.enumerations;

import org.bukkit.Bukkit;

public enum MinecraftVersion {
    v1_8("1_8", 0),
    v1_9("1_9", 1),
    v1_10("1_10", 2),
    v1_11("1_11", 3),
    v1_12("1_12", 4),
    v1_13("1_13", 5),
    v1_14("1_14", 6),
    v1_15("1_15", 7),
    v1_16("1_16", 8),
    v1_17("1_17", 9),
    v1_18("1_18", 10),
    v1_19("1_19", 11),
    v1_20("1_20", 12);

    private final int order;
    private final String key;
    private static MinecraftVersion currentVersion;

    MinecraftVersion(String key, int v) {
        this.key = key;
        order = v;
    }

    public static MinecraftVersion get() {
        if (currentVersion != null) {
            return currentVersion;
        }

        String fullVersion = Bukkit.getVersion();
        String[] splitVersion = fullVersion.split("\\(MC: ");
        if (splitVersion.length > 1) {
            String mcVersion = splitVersion[1].replace(")", "").trim();
            String[] parts = mcVersion.split("\\.");
            if (parts.length >= 2) {
                String versionKey = parts[0] + "_" + parts[1];
                for (MinecraftVersion version : MinecraftVersion.values()) {
                    if (versionKey.equals(version.key)) {
                        currentVersion = version;
                        return version;
                    }
                }
            }
        }

        return null;
    }

    public boolean greaterThanOrEqualTo(MinecraftVersion other) {
        return order >= other.order;
    }

    public boolean lessThanOrEqualTo(MinecraftVersion other) {
        return order <= other.order;
    }
}
