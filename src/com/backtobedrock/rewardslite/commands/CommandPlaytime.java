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
    public void run() {
        if (this.args.length == 0) {
            if (!this.hasPermission()) {
                return;
            }

            if (!this.isPlayer()) {
                return;
            }

            this.showPlaytime(this.sender, MinecraftVersion.get());
        } else if (this.args.length == 1) {
            if (!this.cs.hasPermission("rewardslite.playtime.other")) {
                return;
            }

            this.hasTarget(this.args[0])
                    .thenAcceptAsync(hasTarget -> {
                        if (!hasTarget) {
                            return;
                        }

                        //Player has to be online in 1.15 and below
                        MinecraftVersion minecraftVersion = MinecraftVersion.get();
                        if (minecraftVersion != null && minecraftVersion.lessThanOrEqualTo(MinecraftVersion.v1_15)) {
                            Player onlineTarget = this.isTargetOnline();
                            if (onlineTarget == null) {
                                return;
                            }

                            this.showPlaytime(onlineTarget, minecraftVersion);
                            return;
                        }

                        this.showPlaytime(this.target, minecraftVersion);
                    })
                    .exceptionally(ex -> {
                        ex.printStackTrace();
                        return null;
                    });
        }
    }

    private void showPlaytime(OfflinePlayer commandTarget, MinecraftVersion minecraftVersion) {
        this.plugin.getPlayerRepository().getByPlayer(commandTarget)
                .thenAcceptAsync(playerData ->
                        this.cs.sendMessage(
                                this.plugin.getMessages().getPlaytime(commandTarget.getName(),
                                        MessageUtils.getTimeFromTicks(plugin.getConfigurations().getGeneralConfiguration().isCountPreviousTowardsPlaytime()
                                                && minecraftVersion != null
                                                && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_13)
                                                ? commandTarget.getStatistic(Statistic.PLAY_ONE_MINUTE)
                                                : playerData.getRawPlaytime(), TimePattern.LONG),
                                        MessageUtils.getTimeFromTicks(playerData.getAfkTime(), TimePattern.LONG))))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    //1.15 and below only
    private void showPlaytime(Player commandTarget, MinecraftVersion minecraftVersion) {
        this.plugin.getPlayerRepository().getByPlayer(commandTarget)
                .thenAcceptAsync(playerData ->
                        this.cs.sendMessage(
                                this.plugin.getMessages().getPlaytime(commandTarget.getName(),
                                        MessageUtils.getTimeFromTicks(plugin.getConfigurations().getGeneralConfiguration().isCountPreviousTowardsPlaytime()
                                                && minecraftVersion != null
                                                && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_13)
                                                ? commandTarget.getStatistic(Statistic.PLAY_ONE_MINUTE)
                                                : playerData.getRawPlaytime(), TimePattern.LONG),
                                        MessageUtils.getTimeFromTicks(playerData.getAfkTime(), TimePattern.LONG))))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
}
