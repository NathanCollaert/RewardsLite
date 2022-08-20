package com.backtobedrock.rewardslite.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class CommandPlayTop extends AbstractCommand {

    public CommandPlayTop(CommandSender cs, Command command, String[] args) {
        super(cs, command, args);
    }

    @Override
    public void execute() {
        this.setCommandParameters(false, false, 0, 1, null, -1);
        this.canExecute().thenAcceptAsync(canExecute -> {
            if (canExecute) {
                this.executeTopPlaytime();
            }
        });
    }

    private void executeTopPlaytime() {
        int limit = this.plugin.getConfigurations().getGeneralConfiguration().getTopCommandsLimit();
        CompletableFuture<Map<String, Long>> topMapFuture = this.args.length == 1 && !Boolean.parseBoolean(this.args[0]) ? this.plugin.getPlayerRepository().getTopPlaytime(limit) : this.plugin.getPlayerRepository().getTopTotalTime(limit);
        topMapFuture.thenAcceptAsync(topMap -> {
            StringBuilder topMessage = new StringBuilder(this.plugin.getMessages().getTopPlaytimeTitle(limit)).append("\n");
            int position = 1;
            for (Map.Entry<String, Long> entry : topMap.entrySet()) {
                topMessage.append("\n").append(this.plugin.getMessages().getTopPlaytimeLine(position, entry.getKey(), entry.getValue()));
                position++;
            }
            this.cs.sendMessage(topMessage.toString());
        });
    }
}