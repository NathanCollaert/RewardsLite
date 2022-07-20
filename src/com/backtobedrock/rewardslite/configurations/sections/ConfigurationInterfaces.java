package com.backtobedrock.rewardslite.configurations.sections;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.Display;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigurationInterfaces {

    private final String rewardsInterfaceTitle;
    private final Display fillerDisplay;
    private final Display accentDisplay;
    private final Display nextPageDisplay;
    private final Display previousPageDisplay;
    private final Display pageInformationDisplay;

    public ConfigurationInterfaces(
            String rewardsInterfaceTitle,
            Display fillerDisplay,
            Display accentDisplay,
            Display nextPageDisplay,
            Display previousPageDisplay,
            Display pageInformationDisplay) {
        this.rewardsInterfaceTitle = rewardsInterfaceTitle;
        this.fillerDisplay = fillerDisplay;
        this.accentDisplay = accentDisplay;
        this.nextPageDisplay = nextPageDisplay;
        this.previousPageDisplay = previousPageDisplay;
        this.pageInformationDisplay = pageInformationDisplay;
    }

    public static ConfigurationInterfaces deserialize(ConfigurationSection section) {
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);

        String cRewardsInterfaceTitle = section.getString("rewardsInterfaceTitle", "Your Rewards");
        Map<String, Display> cDisplays = new HashMap<>();

        for (String e : Arrays.asList(
                "fillerDisplay",
                "accentDisplay",
                "nextPageDisplay",
                "previousPageDisplay",
                "pageInformationDisplay"
        )) {
            ConfigurationSection displaySection = section.getConfigurationSection(e);
            if (displaySection != null) {
                Display display = Display.deserialize(e, displaySection);
                if (display == null) {
                    return null;
                }
                cDisplays.put(e, display);
            } else {
                plugin.getLogger().log(Level.SEVERE, String.format("%s was not found, plugin is unable to load.", e));
                return null;
            }
        }

        return new ConfigurationInterfaces(
                cRewardsInterfaceTitle,
                cDisplays.get("fillerDisplay"),
                cDisplays.get("accentDisplay"),
                cDisplays.get("nextPageDisplay"),
                cDisplays.get("previousPageDisplay"),
                cDisplays.get("pageInformationDisplay"));
    }

    public Display getFillerDisplay() {
        return fillerDisplay;
    }

    public Display getAccentDisplay() {
        return accentDisplay;
    }

    public Display getNextPageDisplay() {
        return nextPageDisplay;
    }

    public Display getPreviousPageDisplay() {
        return previousPageDisplay;
    }

    public Display getPageInformationDisplay() {
        return pageInformationDisplay;
    }

    public String getRewardsInterfaceTitle(String player) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("player", player);
        }};
        return MessageUtils.replacePlaceholders(rewardsInterfaceTitle, placeholders);
    }
}
