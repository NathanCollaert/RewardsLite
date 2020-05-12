package com.backtobedrock.LitePlaytimeRewards.commands;

import com.backtobedrock.LitePlaytimeRewards.helperClasses.Reward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RewardsGUI;
import java.util.Map;
import java.util.TreeMap;
import static java.util.stream.Collectors.toMap;
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
                    TreeMap<String, Reward> rewards = this.plugin.getFromCRUDCache(sender.getUniqueId()).getRewards();
                    if (rewards.isEmpty()) {
                        cs.sendMessage(this.plugin.getMessages().getNoRewardsAvailable());
                        break;
                    }

                    //Check if exceeding max inventory size
                    if (rewards.size() > 54) {
                        cs.sendMessage(this.plugin.getMessages().getMaxInventExceeded("/rewards <reward>"));
                        break;
                    }

                    //Create GUI and open
                    TreeMap<String, Reward> filteredRewards = rewards.entrySet().stream()
                            .filter(e -> (!e.getValue().getcReward().isUsePermission() || sender.hasPermission("liteplaytimerewards.reward." + e.getKey()))
                            || e.getValue().getAmountPending() > 0
                            || e.getValue().getAmountRedeemed() > 0)
                            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, TreeMap::new));

                    RewardsGUI rewardsGUI = new RewardsGUI(filteredRewards, sender);
                    sender.openInventory(rewardsGUI.getGUI());
                }
                break;
            case 1:
                //check for permission
                if (this.checkIfPlayer() && this.checkPermission("rewards")) {
                    //check if reward available
                    TreeMap<String, Reward> rewards = this.plugin.getFromCRUDCache(sender.getUniqueId()).getRewards().entrySet().stream()
                            .filter(e -> e.getKey().equalsIgnoreCase(this.args[0]))
                            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, TreeMap::new));

                    if (rewards.isEmpty()) {
                        cs.sendMessage(this.plugin.getMessages().getNoSuchReward(this.args[0]));
                        break;
                    }

                    //Create GUI and open
                    RewardsGUI rewardsGUI = new RewardsGUI(rewards, sender);
                    sender.openInventory(rewardsGUI.getGUI());
                }
                break;
            default:
                this.sendUsageMessage("§6/rewards §e[reward]§r: Check info on all your available rewards or a single one.");
                break;
        }
    }

}
