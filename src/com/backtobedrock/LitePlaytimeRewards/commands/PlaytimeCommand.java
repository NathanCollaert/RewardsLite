package com.backtobedrock.LitePlaytimeRewards.commands;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsCRUD;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlaytimeCommand extends LitePlaytimeRewardsCommand {

    public PlaytimeCommand(CommandSender cs, String[] args) {
        super(cs, args);
    }

    @Override
    public void run() {
        switch (this.args.length) {
            case 0:
                //check if player
                if (this.checkIfPlayer() && this.checkPermission("playtime")) {
                    if (this.plugin.getLPRConfig().isCountAllPlaytime()) {
                        this.cs.sendMessage(this.plugin.getMessages().getPlaytime(sender.getStatistic(Statistic.PLAY_ONE_MINUTE)));
                    } else {
                        //get player data
                        LitePlaytimeRewardsCRUD crudplay = this.plugin.getFromCRUDCache(sender.getUniqueId());
                        this.cs.sendMessage(this.plugin.getMessages().getPlaytime(crudplay.getPlaytime() + crudplay.getAfktime()));
                    }
                }
                break;
            case 1:
                //check for permission
                if (this.checkPermission("playtime.other")) {
                    OfflinePlayer plyrplayother = Bukkit.getOfflinePlayer(args[0]);

                    if (this.plugin.getLPRConfig().isCountAllPlaytime() && plyrplayother.isOnline()) {
                        this.cs.sendMessage(this.plugin.getMessages().getPlaytimeOther(((Player) plyrplayother).getStatistic(Statistic.PLAY_ONE_MINUTE), plyrplayother.getName()));
                        break;
                    }

                    //check if player has played on server before
                    if (!LitePlaytimeRewardsCRUD.doesPlayerDataExists(plyrplayother)) {
                        this.cs.sendMessage(this.plugin.getMessages().getNoData(plyrplayother.getName()));
                        break;
                    }

                    //get player data
                    LitePlaytimeRewardsCRUD crudplayother = plyrplayother.isOnline()
                            ? this.plugin.getFromCRUDCache(plyrplayother.getUniqueId())
                            : new LitePlaytimeRewardsCRUD(plyrplayother);
                    this.cs.sendMessage(this.plugin.getMessages().getPlaytimeOther(crudplayother.getPlaytime() + crudplayother.getAfktime(), plyrplayother.getName()));
                }
                break;
            default:
                this.sendUsageMessage("§6/playtime §e[player]§r: Check your current playtime or that of another player.");
                break;
        }
    }
}
