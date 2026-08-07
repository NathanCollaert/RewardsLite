package com.backtobedrock.rewardslite.domain.enumerations;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public enum PluginVersionState {
    OUTDATED("&4There is a new &c%plugin_name% version (&c%latest_released_version%&4) available for download on &cspigotmc.org&4. Consider updating!"),
    LATEST(""),
    DEV_BUILD("&6You are running a development build of §e%plugin_name% &6(&c%current_version%&6), please use with caution and report all bugs.");

    private final String warningMessage;

    PluginVersionState(String warningMessage) {
        this.warningMessage = warningMessage;
    }

    public static PluginVersionState getPluginVersionState(String latestReleasedVersion) {
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);
        String currentVersion = plugin.getDescription().getVersion();
        int comparison = compareVersions(currentVersion, latestReleasedVersion);
        if (comparison == 0) {
            return LATEST;
        } else if (comparison > 0) {
            return DEV_BUILD;
        } else {
            return OUTDATED;
        }
    }

    /**
     * Compares two dot-separated version strings numerically, segment by segment (e.g. "3.2.10" > "3.2.2").
     * Tolerates a leading "v"/"V" and non-numeric suffixes (e.g. "3.2.2-SNAPSHOT"), since version strings
     * from external sources like the SpigotMC update API aren't guaranteed to match plugin.yml's format exactly.
     */
    private static int compareVersions(String versionA, String versionB) {
        int[] a = toVersionSegments(versionA);
        int[] b = toVersionSegments(versionB);
        int length = Math.max(a.length, b.length);
        for (int i = 0; i < length; i++) {
            int partA = i < a.length ? a[i] : 0;
            int partB = i < b.length ? b[i] : 0;
            if (partA != partB) {
                return Integer.compare(partA, partB);
            }
        }
        return 0;
    }

    private static int[] toVersionSegments(String version) {
        String cleaned = version.trim();
        if (cleaned.regionMatches(true, 0, "v", 0, 1)) {
            cleaned = cleaned.substring(1);
        }
        String[] parts = cleaned.split("\\.");
        int[] segments = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            segments[i] = leadingIntOf(parts[i]);
        }
        return segments;
    }

    private static int leadingIntOf(String part) {
        int end = 0;
        while (end < part.length() && Character.isDigit(part.charAt(end))) {
            end++;
        }
        return end == 0 ? 0 : Integer.parseInt(part.substring(0, end));
    }

    public String getWarningMessage() {
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("plugin_name", plugin.getName());
            put("latest_released_version", plugin.getUpdateChecker().getLatestReleasedVersion());
            put("current_version", plugin.getDescription().getVersion());
        }};
        return MessageUtils.replacePlaceholders(this.warningMessage, placeholders);
    }
}
