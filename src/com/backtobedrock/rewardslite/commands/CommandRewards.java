package com.backtobedrock.rewardslite.commands;

import com.backtobedrock.rewardslite.interfaces.InterfaceRewards;
import com.backtobedrock.rewardslite.utilities.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandRewards extends AbstractCommand {
    public CommandRewards(CommandSender cs, Command command, String[] args) {
        super(cs, command, args);
    }

    @Override
    public void execute() {
        this.setCommandParameters(true, false, 0, 1, this.args.length == 1 ? String.format("%s.rewards.other", this.plugin.getName().toLowerCase()) : null, 0);
        if (canExecute()) {
            this.openRewardsGUI(this.args.length == 1 ? this.target : this.sender);
        }
    }

    private void openRewardsGUI(OfflinePlayer commandTarget) {
        this.plugin.getPlayerRepository().getByPlayer(commandTarget)
                .thenAcceptAsync(playerData -> PlayerUtils.openInventory(this.sender, new InterfaceRewards(this.sender, playerData)))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
}
