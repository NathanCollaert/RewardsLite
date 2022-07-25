package com.backtobedrock.rewardslite.commands;

import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import com.backtobedrock.rewardslite.domain.enumerations.TimePattern;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandPlaytime extends AbstractCommand {

    public CommandPlaytime(CommandSender cs, Command command, String[] args) {
        super(cs, command, args);
    }

    @Override
    public void execute() {
        MinecraftVersion minecraftVersion = MinecraftVersion.get();
        this.setCommandParameters(this.args.length == 0, minecraftVersion != null && minecraftVersion.lessThanOrEqualTo(MinecraftVersion.v1_15), 0, 1, this.args.length == 1 ? String.format("%s.playtime.other", this.plugin.getName().toLowerCase()) : null, 0);
        if (this.canExecute()) {
            this.showPlaytime(this.target == null ? this.sender : this.target, minecraftVersion != null && minecraftVersion.lessThanOrEqualTo(MinecraftVersion.v1_15) ? this.onlineTarget : null, minecraftVersion);
        }
    }

    private void showPlaytime(OfflinePlayer commandTarget, Player onlineCommandTarget, MinecraftVersion minecraftVersion) {
        this.plugin.getPlayerRepository().getByPlayer(onlineCommandTarget == null ? commandTarget : onlineCommandTarget)
                .thenAcceptAsync(playerData ->
                        this.cs.sendMessage(
                                this.plugin.getMessages().getPlaytime(commandTarget.getName(),
                                        MessageUtils.getTimeFromTicks(plugin.getConfigurations().getGeneralConfiguration().isCountPreviousTowardsPlaytime()
                                                && minecraftVersion != null
                                                && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_13)
                                                ? onlineCommandTarget == null ? commandTarget.getStatistic(Statistic.PLAY_ONE_MINUTE) : onlineCommandTarget.getStatistic(Statistic.PLAY_ONE_MINUTE)
                                                : playerData.getRawPlaytime(), TimePattern.LONG),
                                        MessageUtils.getTimeFromTicks(playerData.getAfkTime(), TimePattern.LONG))))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
}
