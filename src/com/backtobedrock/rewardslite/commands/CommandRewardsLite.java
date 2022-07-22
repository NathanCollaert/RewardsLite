package com.backtobedrock.rewardslite.commands;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.Reward;
import com.backtobedrock.rewardslite.domain.RewardData;
import com.backtobedrock.rewardslite.interfaces.InterfaceConversion;
import com.backtobedrock.rewardslite.utilities.CommandUtils;
import com.backtobedrock.rewardslite.utilities.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommandYamlParser;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class CommandRewardsLite extends AbstractCommand {
    public CommandRewardsLite(CommandSender cs, Command command, String[] args) {
        super(cs, command, args);
    }

    @Override
    public void run() {
        if (this.args.length == 0) {
            this.executeHelp();
        } else {
            switch (this.args[0].toLowerCase()) {
                case "reload":
                    this.executeReload();
                    break;
                case "help":
                    this.executeHelp();
                    break;
                case "convert":
                    this.executeConvert();
                    break;
                case "give":
                    this.executeGive();
                    break;
                case "reset":
                    this.executeReset();
                    break;
            }
        }
    }

    private void executeReset() {
        if (!this.cs.hasPermission(String.format("%s.reset", this.plugin.getName().toLowerCase()))) {
            return;
        }

        if (this.args.length < 3) {
            this.sendUsageMessage();
            return;
        }

        List<Reward> rewards = new ArrayList<>();
        if (!this.args[2].equals("*")) {
            Reward reward = this.plugin.getRewardsRepository().getByPermission(this.args[2]);
            if (reward == null) {
                this.cs.sendMessage(this.plugin.getMessages().getRewardDoesNotExist(this.args[2]));
                return;
            }
            rewards.add(reward);
        } else {
            rewards = this.plugin.getRewardsRepository().getAll();
        }

        if (rewards.isEmpty()) {
            return;
        }

        List<Reward> finalRewards = rewards;
        if (this.args[1].equals("*")) {
            this.plugin.getPlayerRepository().getAll().thenAcceptAsync(playerData -> {
                playerData.forEach(p -> p.resetRewards(finalRewards));
                this.plugin.getPlayerRepository().onReload();
                this.cs.sendMessage(this.plugin.getMessages().getResetSuccess(finalRewards.size() > 1 ? this.plugin.getMessages().getAllRewards() : finalRewards.get(0).getPermissionId(), this.plugin.getMessages().getEveryone()));
            });
        } else {
            if (!this.hasPlayedBefore(this.args[1])) {
                return;
            }

            this.plugin.getPlayerRepository().getByPlayer(this.target).thenAcceptAsync(playerData -> {
                playerData.resetRewards(finalRewards);
                this.cs.sendMessage(this.plugin.getMessages().getResetSuccess(finalRewards.size() > 1 ? this.plugin.getMessages().getAllRewards() : finalRewards.get(0).getPermissionId(), this.target.getName()));
            });
        }
    }

    private void executeGive() {
        if (!this.cs.hasPermission(String.format("%s.givereward", this.plugin.getName().toLowerCase()))) {
            return;
        }

        if (this.args.length < 3) {
            this.sendUsageMessage();
            return;
        }

        if (!this.hasPlayedBefore(this.args[1])) {
            return;
        }

        Player targetPlayer = this.isTargetOnline();
        if (targetPlayer == null) {
            return;
        }

        Reward reward = this.plugin.getRewardsRepository().getByPermission(this.args[2]);
        if (reward == null) {
            this.cs.sendMessage(this.plugin.getMessages().getRewardDoesNotExist(this.args[2]));
            return;
        }

        this.plugin.getPlayerRepository().getByPlayer(this.target).thenAcceptAsync(playerData -> {
            RewardData rewardData = playerData.getRewardData(reward);
            if (rewardData == null) {
                return;
            }

            int amount = CommandUtils.getPositiveNumberFromString(this.cs, this.args.length > 3 ? this.args[3] : "1");
            if (amount == -1) {
                return;
            }

            Bukkit.getScheduler().runTask(this.plugin, () -> {
                rewardData.decreaseTimeLeft(amount * reward.getRequiredTime() * 1200L, targetPlayer, true);
                this.cs.sendMessage(this.plugin.getMessages().getGiveRewardSuccess(reward.getPermissionId(), targetPlayer.getName(), amount));
            });
        });
    }

    private void executeConvert() {
        if (!this.cs.hasPermission(String.format("%s.convert", this.plugin.getName().toLowerCase()))) {
            return;
        }

        if (!this.isPlayer()) {
            return;
        }

        PlayerUtils.openInventory(this.sender, new InterfaceConversion());
    }

    private void executeReload() {
        if (!this.cs.hasPermission(String.format("%s.reload", this.plugin.getName().toLowerCase()))) {
            return;
        }

        this.plugin.initialize();
        this.cs.sendMessage(this.plugin.getMessages().getReloadSuccess());
    }

    private void executeHelp() {
        if (!this.cs.hasPermission(String.format("%s.help", this.plugin.getName().toLowerCase()))) {
            return;
        }

        List<String> helpMessage = new ArrayList<>();

        helpMessage.add(this.plugin.getMessages().getCommandHelpHeader());
        PluginCommandYamlParser.parse(JavaPlugin.getPlugin(Rewardslite.class)).stream()
                .filter(e -> e.getPermission() != null && cs.hasPermission(e.getPermission()))
                .forEach(e -> helpMessage.add(CommandUtils.getFancyVersion(e)));
        helpMessage.add(this.plugin.getMessages().getCommandHelpFooter());

        this.cs.sendMessage(helpMessage.toArray(new String[0]));
    }
}
