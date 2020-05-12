package com.backtobedrock.LitePlaytimeRewards.commands;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsCRUD;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class AfktimeCommand extends LitePlaytimeRewardsCommand {

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
                    LitePlaytimeRewardsCRUD crudafk = this.plugin.getFromCRUDCache(this.sender.getUniqueId());

                    this.cs.sendMessage(this.plugin.getMessages().getAFKTime(crudafk.getAfktime()));
                }
                break;
            case 1:
                //check for permission
                if (this.checkPermission("afktime.other") && this.checkEssentials()) {
                    //check if player has played on server before
                    OfflinePlayer plyrafkother = Bukkit.getOfflinePlayer(args[0]);

                    if (!LitePlaytimeRewardsCRUD.doesPlayerDataExists(plyrafkother)) {
                        cs.sendMessage(this.plugin.getMessages().getNoData(plyrafkother.getName()));
                        break;
                    }

                    //get player data
                    LitePlaytimeRewardsCRUD crudafkother = plyrafkother.isOnline()
                            ? this.plugin.getFromCRUDCache(plyrafkother.getUniqueId())
                            : new LitePlaytimeRewardsCRUD(plyrafkother);

                    cs.sendMessage(this.plugin.getMessages().getAFKTimeOther(crudafkother.getAfktime(), plyrafkother.getName()));
                }
                break;
            default:
                this.sendUsageMessage("§6/afktime §e[player]§r: Check your current AFK time or that of another player.");
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
