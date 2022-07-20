package com.backtobedrock.rewardslite.utilities;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandUtils {
    public static int getPositiveNumberFromString(CommandSender sender, String number) {
        try {
            int convertedNumber = Integer.parseInt(number);
            if (convertedNumber < 1) {
                sender.sendMessage(String.format("§c%s is not a valid number between 1 and %d.", number, Integer.MAX_VALUE));
                return -1;
            }
            return convertedNumber;
        } catch (NumberFormatException e) {
            sender.sendMessage(String.format("§c%s is not a valid number between 1 and %d.", number, Integer.MAX_VALUE));
            return -1;
        }
    }

    public static String getFancyVersion(Command command) {
        StringBuilder builder = new StringBuilder("§b").append(command.getUsage().replaceFirst("<command>", command.getName()));
        builder.append(" §8§l- §7").append(command.getDescription()).append(".");
        return builder.toString();
    }
}
