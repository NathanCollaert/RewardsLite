package com.backtobedrock.LitePlaytimeRewards.enums;

import org.bukkit.Bukkit;

public enum MinecraftVersion {
    v1_13("1_13", 5),
    v1_14("1_14", 6),
    v1_15("1_15", 7),
    v1_16("1_16", 8),
    v1_17("1_17", 9),
    v1_18("1_18", 10);

    private final int order;
    private final String key;

    MinecraftVersion(String key, int v) {
        this.key = key;
        order = v;
    }

    public boolean greaterThanOrEqualTo(MinecraftVersion other) {
        return order >= other.order;
    }

    public static MinecraftVersion get() {
        for (MinecraftVersion k : MinecraftVersion.values()) {
            if (Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3].contains(k.key)) {
                return k;
            }
        }
        return null;
    }

}
