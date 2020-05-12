package com.backtobedrock.LitePlaytimeRewards.commands;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

public class LprCommand extends LitePlaytimeRewardsCommand {

    private final List<String> commands = Arrays.asList("§6/afktime §e[player]§r: Check your current AFK time or that of another player.",
            "§6/givereward §e<reward> <player> [amount] [broadcast]§r: Force give one of the reward to a player.",
            "§6/lpr help§r: List of LitePlaytimeRewards commands.",
            "§6/lpr reload§r: Reload the config and messages files.",
            "§6/lpr reset §e<reward> <player>§r: Resets the reward time for the given reward for the given player.",
            "§6/playtime §e[player]§r: Check your current playtime or that of another player.",
            "§6/rewards §e[reward]§r: Check info on all your available rewards or a single one.");

    public LprCommand(CommandSender cs, String[] args) {
        super(cs, args);
    }

    @Override
    public void run() {
        switch (this.args.length) {
            case 0:
                //check for permission
                if (this.checkPermission("help")) {
                    this.sendHelpMessage(this.cs);
                }
                break;
            default:
                switch (this.args[0]) {
                    case "reload":
                        if (this.checkPermission("reload")) {
                            if (this.args.length == 1) {
                                //get files
                                File configFile = new File(this.plugin.getDataFolder(), "config.yml");
                                File messageFile = new File(this.plugin.getDataFolder(), "messages.yml");

                                //reload messages
                                this.plugin.setMessages(messageFile);

                                //reload config file
                                YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                                this.plugin.getLPRConfig().setConfig(config);

                                //save all cruds and redo rewards
                                this.plugin.getAllCRUDs().entrySet().stream().forEach(e -> {
                                    e.getValue().saveConfig();
                                    e.getValue().getData();
                                });

                                this.cs.sendMessage(this.plugin.getMessages().getReloadSuccess());
                            } else {
                                this.sendUsageMessage("§6/lpr reload§r: Reload the config and messages files.");
                            }
                        }
                        break;
                    case "help":
                        if (this.checkPermission("help")) {
                            if (this.args.length == 1) {
                                this.sendHelpMessage(this.cs);
                            } else {
                                this.sendUsageMessage("§6/lpr help§r: List of LitePlaytimeRewards commands.");
                            }
                        }
                        break;
                    case "reset":
                        if (this.checkPermission("reset")) {
                            if (this.args.length == 3) {
                                //check if reward exists
                                if (!plugin.getLPRConfig().getRewards().containsKey(args[1].toLowerCase())) {
                                    cs.sendMessage(plugin.getMessages().getNoSuchReward(args[1]));
                                    break;
                                }

                                //check if player online
                                OfflinePlayer plyrReset = Bukkit.getOfflinePlayer(args[2]);
                                if (!plyrReset.isOnline()) {
                                    cs.sendMessage(plugin.getMessages().getNotOnline(plyrReset.getName()));
                                    break;
                                }

                                this.plugin.getFromCRUDCache(plyrReset.getUniqueId()).getRewards().get(args[1].toLowerCase()).resetTimeTillNextReward();

                                this.cs.sendMessage(this.plugin.getMessages().getResetSuccess(plyrReset.getName(), args[1].toLowerCase()));
                            } else {
                                this.sendUsageMessage("§6/lpr reset §e<reward> <player>§r: Resets the reward time for the given reward for the given player.");
                            }
                        }
                        break;
                    default:
                        this.sendUsageMessage("§6/lpr §e[help§f|§ereload§f|§ereset]§r: LPR commands.");
                        break;
                }
                break;
        }
    }

    private void sendHelpMessage(CommandSender cs) {
        StringBuilder sb = new StringBuilder("§bAvailable commands:");
        this.commands.stream().forEach(e -> sb.append("\n").append(e));
        cs.sendMessage(sb.toString());
    }

}
