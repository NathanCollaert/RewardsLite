package com.backtobedrock.LitePlaytimeRewards.commands;

import com.backtobedrock.LitePlaytimeRewards.configs.PlayerData;
import com.backtobedrock.LitePlaytimeRewards.enums.Command;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class AfktimeCommand extends Commands {

    public AfktimeCommand(CommandSender cs, String[] args) {
        super(cs, args);
    }

    @Override
    public void run() {
        switch (this.args.length) {
            case 0:
                //check if player and has permission
                if (this.checkIfPlayer() && this.checkPermission("afktime") && this.checkEssentials()) {
                    //get player data
                    PlayerData crudafk = this.plugin.getPlayerCache().get(this.sender.getUniqueId());

                    this.cs.sendMessage(this.plugin.getMessages().getAFKTime(crudafk.getAfktime()));
                }
                break;
            case 1:
                //check for permission
                if (this.checkPermission("afktime.other") && this.checkEssentials()) {
                    //check if player has played on server before
                    @SuppressWarnings("deprecation") OfflinePlayer plyrafkother = Bukkit.getOfflinePlayer(args[0]);

                    if (!PlayerData.doesPlayerDataExists(plyrafkother)) {
                        cs.sendMessage(this.plugin.getMessages().getNoData(plyrafkother.getName()));
                        break;
                    }

                    //get player data
                    PlayerData crudafkother = plyrafkother.isOnline()
                            ? this.plugin.getPlayerCache().get(plyrafkother.getUniqueId())
                            : new PlayerData(plyrafkother);

                    cs.sendMessage(this.plugin.getMessages().getAFKTimeOther(crudafkother.getAfktime(), plyrafkother.getName()));
                }
                break;
            default:
                this.sendUsageMessage(Command.AFKTIME);
                break;
        }
    }

    private boolean checkEssentials() {
        if (this.plugin.ess == null) {
            this.cs.sendMessage(this.plugin.getMessages().getServerDoesntKeepTrackOfAFK());
            return false;
        }
        return true;
    }

}
