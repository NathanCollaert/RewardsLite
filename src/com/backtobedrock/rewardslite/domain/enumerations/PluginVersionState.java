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
        if (latestReleasedVersion.equals(currentVersion)) {
            return LATEST;
        } else {
            String[] latestReleasedVersionArray = latestReleasedVersion.split("\\.");
            String[] currentVersionArray = currentVersion.split("\\.");
            if (currentVersionArray.length > latestReleasedVersionArray.length) {
                return DEV_BUILD;
            } else {
                return OUTDATED;
            }
        }
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
