package com.backtobedrock.LitePlaytimeRewards.commands;

import com.backtobedrock.LitePlaytimeRewards.enums.Command;
import com.backtobedrock.LitePlaytimeRewards.guis.RewardsGUI;
import com.backtobedrock.LitePlaytimeRewards.models.Reward;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;

public class RewardsCommand extends Commands {

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
                    TreeMap<String, Reward> rewards = this.plugin.getPlayerCache().get(this.sender.getUniqueId()).getRewards().entrySet().stream().filter(e -> e.getValue().hasPermission(this.sender) || e.getValue().getAmountPending() > 0 || e.getValue().getAmountRedeemed() > 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, TreeMap::new));
                    if (rewards.isEmpty()) {
                        this.cs.sendMessage(this.plugin.getMessages().getNoRewardsAvailable());
                        break;
                    }

                    RewardsGUI rewardsGUI = new RewardsGUI(this.plugin.getPlayerCache().get(this.sender.getUniqueId()), rewards);
                    this.sender.openInventory(rewardsGUI.getInventory());
                }
                break;
            default:
                this.sendUsageMessage(Command.REWARDS);
                break;
        }
    }

}
