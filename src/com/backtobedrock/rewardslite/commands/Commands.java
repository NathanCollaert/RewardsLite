package com.backtobedrock.rewardslite.commands;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.Reward;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.HumanEntity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Commands implements TabCompleter {
    private final Rewardslite plugin;

    public Commands() {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        Collections.singletonList("rewardslite").forEach(this::registerPluginCommand);
    }

    private void registerPluginCommand(String command) {
        PluginCommand pluginCommand = Bukkit.getServer().getPluginCommand(command);
        if (pluginCommand != null) {
            pluginCommand.setTabCompleter(this);
        }
    }

    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        switch (cmnd.getName().toLowerCase()) {
            case "playtime":
                new CommandPlaytime(cs, cmnd, args).execute();
                break;
            case "rewards":
                new CommandRewards(cs, cmnd, args).execute();
                break;
            case "rewardslite":
                new CommandRewardsLite(cs, cmnd, args).execute();
                break;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmnd, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        switch (cmnd.getName().toLowerCase()) {
            case "rewardslite":
                switch (args.length) {
                    case 1:
                        StringUtil.copyPartialMatches(args[0].toLowerCase(), Arrays.asList("convert", "give", "help", "reload", "reset"), completions);
                        Collections.sort(completions);
                        break;
                    case 2:
                        if (Arrays.asList("give", "reset").contains(args[0])) {
                            List<String> players = this.plugin.getServer().getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toList());
                            if (args[0].equals("reset")) {
                                players.add("*");
                            }
                            StringUtil.copyPartialMatches(args[1].toLowerCase(), players, completions);
                            Collections.sort(completions);
                        }
                        break;
                    case 3:
                        List<String> rewards = this.plugin.getRewardsRepository().getAll().stream().map(Reward::getPermissionId).collect(Collectors.toList());
                        if (args[0].equals("reset")) {
                            rewards.add("*");
                        }
                        StringUtil.copyPartialMatches(args[2].toLowerCase(), rewards, completions);
                        Collections.sort(completions);
                        break;
                }
                break;
        }

        return completions;
    }
}
