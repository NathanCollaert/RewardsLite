package com.backtobedrock.rewardslite.commands;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.utilities.CommandUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractCommand {

    protected final Rewardslite plugin;
    protected final CommandSender cs;
    protected final Command command;
    protected final String[] args;
    protected Player sender = null;
    protected OfflinePlayer target = null;
    protected Player onlineTarget = null;

    protected boolean requiresOnlineSender = false;
    protected boolean requiresOnlineTarget = false;
    protected int minRequiredArguments = 0;
    protected int maxRequiredArguments = 0;

    protected String externalPermission = null;
    protected int targetArgumentPosition = -1;

    public AbstractCommand(CommandSender cs, Command command, String[] args) {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.cs = cs;
        this.command = command;
        this.args = args;
    }

    public CompletableFuture<Boolean> canExecute() {
        return CompletableFuture.supplyAsync(() -> {
            if (!this.hasPermission()) {
                return false;
            }

            if (this.requiresOnlineSender && !this.isSenderOnline()) {
                return false;
            }

            if (this.args.length < this.minRequiredArguments || this.args.length > this.maxRequiredArguments) {
                this.sendUsageMessage();
                return false;
            }

            if (this.targetArgumentPosition != -1 && this.args.length > this.targetArgumentPosition && !this.hasPlayedBefore(this.args[this.targetArgumentPosition])) {
                return false;
            }

            return !this.requiresOnlineTarget || this.isTargetOnline();
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    public abstract void execute();

    protected boolean hasPermission() {
        if (this.externalPermission != null) {
            boolean hasPermission = this.cs.hasPermission(this.externalPermission);
            if (!hasPermission) {
                this.cs.sendMessage(this.plugin.getMessages().getNoPermissionError());
            }
            return hasPermission;
        }
        return this.command.testPermission(this.cs);
    }

    protected boolean isSenderOnline() {
        if (!(this.cs instanceof Player)) {
            this.cs.sendMessage(this.plugin.getMessages().getRequireOnlinePlayerError());
            return false;
        }
        this.sender = (Player) this.cs;
        return true;
    }

    protected boolean isTargetOnline() {
        if (this.target == null) {
            return false;
        }

        if (this.target.getPlayer() == null) {
            this.cs.sendMessage(this.plugin.getMessages().getTargetNotOnlineError(this.target.getName()));
            return false;
        }

        this.onlineTarget = this.target.getPlayer();
        return true;
    }

    protected boolean hasPlayedBefore(String playerName) {
        OfflinePlayer player = Arrays.stream(this.plugin.getServer().getOfflinePlayers()).filter(e -> e.getName() != null && e.getName().equalsIgnoreCase(playerName)).findFirst().orElse(this.plugin.getServer().getPlayer(playerName));
        if (player == null) {
            this.cs.sendMessage(this.plugin.getMessages().getTargetNotPlayedBeforeError(playerName));
            return false;
        }
        this.target = player;
        return true;
    }

    public void sendUsageMessage() {
        String[] message = new String[]{this.plugin.getMessages().getCommandUsageHeader(), CommandUtils.getFancyVersion(this.command), this.plugin.getMessages().getCommandUsageFooter()};
        this.cs.sendMessage(message);
    }

    public AbstractCommand setRequiresOnlineSender(boolean requiresOnlineSender) {
        this.requiresOnlineSender = requiresOnlineSender;
        return this;
    }

    public AbstractCommand setRequiresOnlineTarget(boolean requiresOnlineTarget) {
        this.requiresOnlineTarget = requiresOnlineTarget;
        return this;
    }

    public AbstractCommand setMinRequiredArguments(int minRequiredArguments) {
        this.minRequiredArguments = minRequiredArguments;
        return this;
    }

    public AbstractCommand setMaxRequiredArguments(int maxRequiredArguments) {
        this.maxRequiredArguments = maxRequiredArguments;
        return this;
    }

    public AbstractCommand setExternalPermission(String externalPermission) {
        this.externalPermission = externalPermission;
        return this;
    }

    public AbstractCommand setTargetArgumentPosition(int targetArgumentPosition) {
        this.targetArgumentPosition = targetArgumentPosition;
        return this;
    }
}
