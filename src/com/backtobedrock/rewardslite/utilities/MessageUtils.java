package com.backtobedrock.rewardslite.utilities;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import com.backtobedrock.rewardslite.domain.enumerations.TimePattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageUtils {
    public static final DateTimeFormatter SHORT_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yy',' HH:mm z").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter MEDIUM_FORMATTER = DateTimeFormatter.ofPattern("MMM dd yyyy',' HH:mm z").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter LONG_FORMATTER = DateTimeFormatter.ofPattern("EEEE MMM dd yyyy 'at' HH:mm:ss z").withZone(ZoneId.systemDefault());
    private static final Pattern hexPattern = Pattern.compile("&#([0-9a-fA-F]){6}|&#([0-9a-fA-F]){3}");

    public static long timeUnitToTicks(long time, TimeUnit unit) {
        return unit.toSeconds(time) * 20;
    }

    public static long timeBetweenDatesToTicks(LocalDateTime date1, LocalDateTime date2) {
        return Math.abs(ChronoUnit.SECONDS.between(date1, date2)) * 20;
    }

    public static String getTimeFromTicks(long amount, TimePattern pattern) {
        Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);
        StringBuilder sb = new StringBuilder();

        long w = 0, d = 0, h = 0, m = 0, s = 0;

        switch (plugin.getConfigurations().getGeneralConfiguration().getTimeUnitLimit()) {
            case WEEKS:
                w = amount / 12096000;
                d = amount % 12096000 / 1728000;
                h = amount % 1728000 / 72000;
                m = amount % 72000 / 1200;
                s = amount % 1200 / 20;
                break;
            case DAYS:
                d = amount / 1728000;
                h = amount % 1728000 / 72000;
                m = amount % 72000 / 1200;
                s = amount % 1200 / 20;
                break;
            case HOURS:
                h = amount / 72000;
                m = amount % 72000 / 1200;
                s = amount % 1200 / 20;
                break;
            case MINUTES:
                m = amount / 1200;
                s = amount % 1200 / 20;
                break;
            case SECONDS:
                s = amount / 20;
                break;
        }

        switch (pattern) {
            case LONG:
                if (w > 0) {
                    sb.append(w).append(" ").append(w == 1 ? plugin.getMessages().getWeek() : plugin.getMessages().getWeeks());
                }
                if (d > 0) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(", ");
                    }
                    sb.append(d).append(" ").append(d == 1 ? plugin.getMessages().getDay() : plugin.getMessages().getDays());
                }
                if (h > 0) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(", ");
                    }
                    sb.append(h).append(" ").append(h == 1 ? plugin.getMessages().getHour() : plugin.getMessages().getHours());
                }
                if (m > 0) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(", ");
                    }
                    sb.append(m).append(" ").append(m == 1 ? plugin.getMessages().getMinute() : plugin.getMessages().getMinutes());
                }
                if (s > 0 || (d == 0 && h == 0 && m == 0)) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(", ");
                    }
                    sb.append(s).append(" ").append(s == 1 ? plugin.getMessages().getSecond() : plugin.getMessages().getSeconds());
                }
                break;
            case SHORT:
                if (w > 0) {
                    sb.append(w).append(plugin.getMessages().getWeeks().charAt(0));
                }
                if (d > 0) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(", ");
                    }
                    sb.append(d).append(plugin.getMessages().getDays().charAt(0));
                }
                if (h > 0) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(", ");
                    }
                    sb.append(h).append(plugin.getMessages().getHours().charAt(0));
                }
                if (m > 0) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(", ");
                    }
                    sb.append(m).append(plugin.getMessages().getMinutes().charAt(0));
                }
                if (s > 0 || (d == 0 && h == 0 && m == 0)) {
                    if (!sb.toString().isEmpty()) {
                        sb.append(", ");
                    }
                    sb.append(s).append(plugin.getMessages().getSeconds().charAt(0));
                }
                break;
            case DIGITAL:
                sb.append(w > 9 ? w : "0" + w).append(d > 9 ? d : "0" + d).append(":").append(h > 9 ? h : "0" + h).append(":").append(m > 9 ? m : "0" + m).append(":").append(s > 9 ? s : "0" + s);
                break;
        }

        return sb.toString();
    }

    public static String replacePlaceholders(String string, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            string = string.replaceAll("%" + entry.getKey().toLowerCase() + "%", entry.getValue());
        }
        return applyColor(string);
    }

    public static ItemStack replaceItemNamePlaceholders(ItemStack item, Map<String, String> placeholders) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(replacePlaceholders(itemMeta.getDisplayName(), placeholders));
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    public static ItemStack replaceItemLorePlaceholders(ItemStack item, Map<String, String> placeholders) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null && itemMeta.getLore() != null) {
            itemMeta.setLore(itemMeta.getLore().stream().map(e -> replacePlaceholders(e, placeholders)).collect(Collectors.toList()));
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    public static ItemStack replaceItemNameAndLorePlaceholders(ItemStack item, Map<String, String> placeholders) {
        return replaceItemNamePlaceholders(replaceItemLorePlaceholders(item, placeholders), placeholders);
    }

    public static String applyColor(String message) {
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer sb = new StringBuffer();
        MinecraftVersion minecraftVersion = MinecraftVersion.get();
        if (minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_16)) {
            while (matcher.find()) {
                String hex = matcher.group();
                if (hex.length() == 5) {
                    hex = hex.substring(0, 2) + doubleCharacters(hex.substring(2));
                }
                matcher.appendReplacement(sb, ChatColor.of(hex.substring(1)).toString());
            }
        }
        matcher.appendTail(sb);
        return ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    private static String doubleCharacters(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            sb.append(c);
            sb.append(c);
        }
        return sb.toString();
    }
}
