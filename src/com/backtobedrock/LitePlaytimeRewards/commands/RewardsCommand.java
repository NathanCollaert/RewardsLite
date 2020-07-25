package com.backtobedrock.LitePlaytimeRewards.commands;

import com.backtobedrock.LitePlaytimeRewards.guis.RewardsGUI;
import com.backtobedrock.LitePlaytimeRewards.models.Reward;
import java.util.TreeMap;
import org.bukkit.command.CommandSender;

public class RewardsCommand extends LitePlaytimeRewardsCommand {

    public RewardsCommand(CommandSender cs, String[] args) {
        super(cs, args);
    }

    @Override
    public void run() {
        switch (this.args.length) {
            case 0:
                //check for permission
                if (this.checkIfPlayer() && this.checkPermission("rewards")) {
                    //check if rewards available
                    TreeMap<String, Reward> rewards = this.plugin.getPlayerCache().get(this.sender.getUniqueId()).getRewards();
                    if (rewards.isEmpty()) {
                        this.cs.sendMessage(this.plugin.getMessages().getNoRewardsAvailable());
                        break;
                    }

                    RewardsGUI rewardsGUI = new RewardsGUI(this.plugin.getPlayerCache().get(this.sender.getUniqueId()));
                    this.sender.openInventory(rewardsGUI.getInventory());
                }
                break;
            default:
                this.sendUsageMessage(Commands.REWARDS);
                break;
        }
    }

}
