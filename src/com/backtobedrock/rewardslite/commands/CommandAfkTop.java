package com.backtobedrock.rewardslite.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class CommandAfkTop extends AbstractCommand {
    public CommandAfkTop(CommandSender cs, Command command, String[] args) {
        super(cs, command, args);
    }

    @Override
    public void execute() {
        this.setCommandParameters(false, false, 0, 0, null, -1);
        if (canExecute()) {
            this.executeTopAfkTime();
        }
    }

    private void executeTopAfkTime() {
        int limit = this.plugin.getConfigurations().getGeneralConfiguration().getTopCommandsLimit();
        this.plugin.getPlayerRepository().getTopAfkTime(limit).thenAcceptAsync(topMap -> {
            StringBuilder topMessage = new StringBuilder(this.plugin.getMessages().getTopAfkTimeTitle(limit)).append("\n");
            int position = 1;
            for (Map.Entry<String, Long> entry : topMap.entrySet()) {
                topMessage.append("\n").append(this.plugin.getMessages().getTopAfkTimeLine(position, entry.getKey(), entry.getValue()));
                position++;
            }
            this.cs.sendMessage(topMessage.toString());
        });
    }
}