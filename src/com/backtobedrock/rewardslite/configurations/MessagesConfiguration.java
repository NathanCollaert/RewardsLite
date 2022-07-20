package com.backtobedrock.rewardslite.configurations;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MessagesConfiguration {
    private final Rewardslite plugin;
    private final FileConfiguration messages;

    public MessagesConfiguration(File messagesFile) {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    //<editor-fold desc="Translations" defaultstate="collapsed">
    public String getDays() {
        return MessageUtils.replacePlaceholders(this.messages.getString("days", "days"), Collections.emptyMap());
    }

    public String getHours() {
        return MessageUtils.replacePlaceholders(this.messages.getString("hours", "hours"), Collections.emptyMap());
    }

    public String getMinutes() {
        return MessageUtils.replacePlaceholders(this.messages.getString("minutes", "minutes"), Collections.emptyMap());
    }

    public String getSeconds() {
        return MessageUtils.replacePlaceholders(this.messages.getString("seconds", "seconds"), Collections.emptyMap());
    }

    public String getDay() {
        return MessageUtils.replacePlaceholders(this.messages.getString("day", "day"), Collections.emptyMap());
    }

    public String getHour() {
        return MessageUtils.replacePlaceholders(this.messages.getString("hour", "hour"), Collections.emptyMap());
    }

    public String getMinute() {
        return MessageUtils.replacePlaceholders(this.messages.getString("minute", "minute"), Collections.emptyMap());
    }

    public String getSecond() {
        return MessageUtils.replacePlaceholders(this.messages.getString("second", "second"), Collections.emptyMap());
    }

    public String getEveryone() {
        return MessageUtils.replacePlaceholders(this.messages.getString("everyone", "everyone"), Collections.emptyMap());
    }

    public String getAllRewards() {
        return MessageUtils.replacePlaceholders(this.messages.getString("allRewards", "all rewards"), Collections.emptyMap());
    }
    //</editor-fold>

    //<editor-fold desc="Rewards" defaultstate="collapsed">
    public String getDisabledWorldRedeemWarning(String rewardTitle, String disabledWorld) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("reward_title", rewardTitle);
            put("disabled_world", disabledWorld);
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("disabledWorldRedeemWarning", "&eThe %reward_title% reward cannot be redeemed in this world."), placeholders);
    }

    public String getInventoryFullRedeemWarning(String rewardTitle, String requiredSpaces) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("reward_title", rewardTitle);
            put("required_spaces", requiredSpaces);
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("inventoryFullRedeemWarning", "&eThe %reward_title% reward requires %required_spaces% free inventory spaces to be redeemed."), placeholders);
    }
    //</editor-fold>

    //<editor-fold desc="Commands" defaultstate="collapsed">
    public String getRequireOnlinePlayerError() {
        return MessageUtils.applyColor(this.messages.getString("requireOnlinePlayerError", "&cYou will need to log in to use this command."));
    }

    public String getTargetNotOnlineError(String player) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("player", player);
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("targetNotOnlineError", "&c%player% is currently not online."), placeholders);
    }

    public String getTargetNotPlayedBeforeError(String player) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("player", player);
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("targetNotPlayedBeforeError", "&c%player% has not played on the server before."), placeholders);
    }

    public String getRewardDoesNotExist(String reward) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("reward", reward);
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("rewardDoesNotExist", "&c%reward% does not exist on this server."), placeholders);
    }

    public String getCommandUsageHeader() {
        return MessageUtils.applyColor(this.messages.getString("commandUsageHeader", "&8&m--------------&6 Command &fUsage &8&m--------------"));
    }

    public String getCommandUsageFooter() {
        return MessageUtils.applyColor(this.messages.getString("commandUsageFooter", "&8&m------------------------------------------"));
    }

    public String getCommandHelpHeader() {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("plugin_name", plugin.getName());
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("commandHelpHeader", "&8&m----------&6 %plugin_name% &fHelp &8&m----------"), placeholders);
    }

    public String getCommandHelpFooter() {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("plugin_name", plugin.getName());
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("commandHelpFooter", "&8&m------------------------------------------"), placeholders);
    }

    public String getReloadSuccess() {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("plugin_name", plugin.getName());
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("reloadSuccess", "&aSuccessfully reloaded %plugin_name%."), placeholders);
    }

    public String getGiveRewardSuccess(String reward, String player, int amount) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("reward", reward);
            put("player", player);
            put("amount", Integer.toString(amount));
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("giveRewardSuccess", "&aSuccessfully given %reward% to %player% (x%amount%)."), placeholders);
    }

    public String getResetSuccess(String reward, String player) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("reward", reward);
            put("player", player);
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("resetSuccess", "&aSuccessfully reset %reward% for %player%."), placeholders);
    }

    public String getPlaytime(String player, String playtime, String afkTime) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("player", player);
            put("playtime", playtime);
            put("afk_time", afkTime);
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("playtime", "&e%player% have played for &6%playtime%&e of which &6%afk_time%&e were AFK."), placeholders);
    }
    //</editor-fold>
}
