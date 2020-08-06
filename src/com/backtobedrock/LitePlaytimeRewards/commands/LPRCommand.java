package com.backtobedrock.LitePlaytimeRewards.commands;

import com.backtobedrock.LitePlaytimeRewards.enums.Command;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class LPRCommand extends Commands {

    public LPRCommand(CommandSender cs, String[] args) {
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
                                this.plugin.initialize();

                                //save all cruds and redo rewards
                                this.plugin.getPlayerCache().entrySet().stream().forEach(e -> {
                                    e.getValue().saveConfig();
                                    e.getValue().getData();
                                });

                                this.cs.sendMessage(this.plugin.getMessages().getReloadSuccess());
                            } else {
                                this.sendUsageMessage(Command.LPR_RELOAD);
                            }
                        }
                        break;
                    case "help":
                        if (this.checkPermission("help")) {
                            if (this.args.length == 1) {
                                this.sendHelpMessage(this.cs);
                            } else {
                                this.sendUsageMessage(Command.LPR_HELP);
                            }
                        }
                        break;
                    case "reset":
                        if (this.checkPermission("reset")) {
                            if (this.args.length == 3) {
                                //check if reward exists
                                if (!this.plugin.getRewards().getAll().containsKey(args[1].toLowerCase())) {
                                    this.cs.sendMessage(plugin.getMessages().getNoSuchReward(args[1]));
                                    break;
                                }

                                //check if player online
                                OfflinePlayer plyrReset = Bukkit.getOfflinePlayer(args[2]);
                                if (!plyrReset.isOnline()) {
                                    this.cs.sendMessage(plugin.getMessages().getNotOnline(plyrReset.getName()));
                                    break;
                                }

                                this.plugin.getPlayerCache().get(plyrReset.getUniqueId()).getRewards().get(args[1].toLowerCase()).resetTimeTillNextReward();

                                this.cs.sendMessage(this.plugin.getMessages().getResetSuccess(plyrReset.getName(), args[1].toLowerCase()));
                            } else {
                                this.sendUsageMessage(Command.LPR_RESET);
                            }
                        }
                        break;
                    default:
                        this.sendUsageMessage(Command.LPR);
                        break;
                }
                break;
        }
    }

    private void sendHelpMessage(CommandSender cs) {
        List<String> helpMessage = new ArrayList<>();
        helpMessage.add("§8§m----------§6 LitePlaytimeRewards §fHelp §8§m----------");
        Arrays.stream(Command.values()).forEach(e -> helpMessage.add(e.getFancyVersion()));
        helpMessage.add("§8§m------------------------------------------");
        cs.sendMessage(Arrays.stream(helpMessage.toArray()).toArray(String[]::new));
    }

}
