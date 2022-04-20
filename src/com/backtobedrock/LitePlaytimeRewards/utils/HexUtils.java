package com.backtobedrock.LitePlaytimeRewards.utils;

import com.backtobedrock.LitePlaytimeRewards.enums.MinecraftVersion;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexUtils {

    private static final Pattern hexPattern = Pattern.compile("&#([0-9a-fA-F]){6}|&#([0-9a-fA-F]){3}");

    /**
     * Translates the given List to RGB-supported value. Applies the corresponding color as well.
     *
     * @param messages - The original list of strings to be converted.
     * @return List<Component> - The new Component List with color codes.
     */
    public static List<String> applyColor(List<String> messages) {
        List<String> buffered = new ArrayList<>();
        for (String message : messages) {
            buffered.add(applyColor(message));
        }
        return buffered;
    }

    /**
     * Translates the given String to RGB-supported value. Applies the corresponding color as well.
     * Example format: &#FFFFF, &7
     *
     * @param message - The original string to be converted.
     * @return String - Translated String with Color Codes
     */
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
        return ChatColor.RESET + ChatColor.translateAlternateColorCodes('&', sb.toString());
    }

    // Helper function to append double chars.
    private static String doubleCharacters(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            sb.append(c);
            sb.append(c);
        }
        return sb.toString();
    }
}