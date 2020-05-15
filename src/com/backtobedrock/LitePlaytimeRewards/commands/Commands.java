package com.backtobedrock.LitePlaytimeRewards.commands;

public enum Commands {
    AFKTIME("/afktime §6§o[player]", "Check your current AFK time or that of another player."),
    GIVEREWARD("/givereward §6<reward> <player> §o[amount] [broadcast]", "Force give one of the reward to a player."),
    LPR("/lpr §6§o[help §8|§6§o reload §8|§6§o reset]", "LPR commands."),
    LPR_HELP("/lpr help", "List of LitePlaytimeRewards commands."),
    LPR_RELOAD("/lpr reload", "Reload the config and messages files."),
    LPR_RESET("/lpr reset §6<reward> <player>", "Resets the reward time for the given reward for the given player."),
    PLAYTIME("/playtime §6§o[player]", "Check your current playtime or that of another player."),
    REWARDS("/rewards §6§o[reward]", "Check info on all your available rewards or a single one.");

    private final String usage;
    private final String description;

    private Commands(String usage, String description) {
        this.usage = usage;
        this.description = description;
    }

    public String getUsage() {
        return this.usage;
    }

    public String getDescription() {
        return this.description;
    }

    public String getFancyVersion() {
        return "§e" + this.usage + " §8§l- §7" + this.description;
    }
}
