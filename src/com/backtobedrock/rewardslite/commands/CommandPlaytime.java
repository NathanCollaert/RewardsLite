package com.backtobedrock.rewardslite.commands;

import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import com.backtobedrock.rewardslite.domain.enumerations.TimePattern;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandPlaytime extends AbstractCommand {

    public CommandPlaytime(CommandSender cs, Command command, String[] args) {
        super(cs, command, args);
    }

    @Override
    public void execute() {
        MinecraftVersion minecraftVersion = MinecraftVersion.get();
        boolean isLessOrEqualTo1_15 = minecraftVersion != null && minecraftVersion.lessThanOrEqualTo(MinecraftVersion.v1_15);
        this.setRequiresOnlineSender(this.args.length == 0)
                .setRequiresOnlineTarget(isLessOrEqualTo1_15)
                .setMaxRequiredArguments(1)
                .setExternalPermission(this.args.length == 1 ? String.format("%s.playtime.other", this.plugin.getName().toLowerCase()) : null)
                .setTargetArgumentPosition(0)
                .canExecute()
                .thenAcceptAsync(canExecute -> {
                    if (canExecute) {
                        this.showPlaytime(this.args.length == 0 ? this.sender : isLessOrEqualTo1_15 ? this.onlineTarget : this.target);
                    }
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    private void showPlaytime(OfflinePlayer target) {
        this.plugin.getPlayerRepository().getByPlayer(target).thenAcceptAsync(playerData -> {
            String message = this.plugin.getMessages().getPlaytime(target.getName(),
                    MessageUtils.getTimeFromTicks(this.plugin.getConfigurations().getGeneralConfiguration().isCountPreviousTowardsPlaytime()
                            ? Math.min(target.getStatistic(Statistic.PLAY_ONE_MINUTE) - playerData.getAfkTime(), target.getStatistic(Statistic.PLAY_ONE_MINUTE))
                            : playerData.getPlaytime(), TimePattern.LONG),
                    MessageUtils.getTimeFromTicks(playerData.getAfkTime(), TimePattern.LONG)
            );
            this.cs.sendMessage(message);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }
}
