package com.backtobedrock.rewardslite.configurations;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.enumerations.TimePattern;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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
    public String getWeeks() {
        return MessageUtils.applyColor(this.messages.getString("weeks", "weeks"));
    }

    public String getDays() {
        return MessageUtils.applyColor(this.messages.getString("days", "days"));
    }

    public String getHours() {
        return MessageUtils.applyColor(this.messages.getString("hours", "hours"));
    }

    public String getMinutes() {
        return MessageUtils.applyColor(this.messages.getString("minutes", "minutes"));
    }

    public String getSeconds() {
        return MessageUtils.applyColor(this.messages.getString("seconds", "seconds"));
    }

    public String getWeek() {
        return MessageUtils.applyColor(this.messages.getString("week", "week"));
    }

    public String getDay() {
        return MessageUtils.applyColor(this.messages.getString("day", "day"));
    }

    public String getHour() {
        return MessageUtils.applyColor(this.messages.getString("hour", "hour"));
    }

    public String getMinute() {
        return MessageUtils.applyColor(this.messages.getString("minute", "minute"));
    }

    public String getSecond() {
        return MessageUtils.applyColor(this.messages.getString("second", "second"));
    }

    public String getEveryone() {
        return MessageUtils.applyColor(this.messages.getString("everyone", "everyone"));
    }

    public String getAllRewards() {
        return MessageUtils.applyColor(this.messages.getString("allRewards", "all rewards"));
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


    public String getTopPlaytimeTitle(int limit) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("entries_amount", Integer.toString(limit));
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("topPlaytimeTitle", "&eTop &6%entries_amount% &eplayers &6playtime&e:"), placeholders);
    }

    public String getTopAfkTimeTitle(int limit) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("entries_amount", Integer.toString(limit));
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("topAfkTimeTitle", "&eTop &6%entries_amount% &eplayers &6AFK time&e:"), placeholders);
    }

    public String getTopPlaytimeLine(int position, String player, long playtime) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("position", Integer.toString(position));
            put("player", player);
            put("playtime_long", MessageUtils.getTimeFromTicks(playtime, TimePattern.LONG));
            put("playtime_short", MessageUtils.getTimeFromTicks(playtime, TimePattern.SHORT));
            put("playtime_digital", MessageUtils.getTimeFromTicks(playtime, TimePattern.DIGITAL));
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("topPlaytimeLine", "&9%position%. &b%player%: &e%playtime_short%"), placeholders);
    }

    public String getTopAfkTimeLine(int position, String player, long afkTime) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("position", Integer.toString(position));
            put("player", player);
            put("afk_time_long", MessageUtils.getTimeFromTicks(afkTime, TimePattern.LONG));
            put("afk_time_short", MessageUtils.getTimeFromTicks(afkTime, TimePattern.SHORT));
            put("afk_time_digital", MessageUtils.getTimeFromTicks(afkTime, TimePattern.DIGITAL));
        }};
        return MessageUtils.replacePlaceholders(this.messages.getString("topAfkTimeLine", "&9%position%. &b%player%: &e%afk_time_short%"), placeholders);
    }
    //</editor-fold>
}