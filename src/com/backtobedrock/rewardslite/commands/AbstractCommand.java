package com.backtobedrock.rewardslite.commands;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.utilities.CommandUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractCommand {

    protected final Rewardslite plugin;
    protected final CommandSender cs;
    protected final Command command;
    protected final String[] args;
    protected Player sender = null;
    protected OfflinePlayer target = null;

    public AbstractCommand(CommandSender cs, Command command, String[] args) {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.cs = cs;
        this.command = command;
        this.args = args;
    }

    public abstract void run();

    protected boolean hasPermission() {
        return this.command.testPermission(this.cs);
    }

    protected boolean isPlayer() {
        if (!(this.cs instanceof Player)) {
            this.cs.sendMessage(this.plugin.getMessages().getRequireOnlinePlayerError());
            return false;
        }
        this.sender = (Player) cs;
        return true;
    }

    protected Player isTargetOnline() {
        if (this.target == null) {
            return null;
        }

        if (this.target.getPlayer() == null) {
            this.cs.sendMessage(this.plugin.getMessages().getTargetNotOnlineError(this.target.getName()));
        }

        return this.target.getPlayer();
    }

    protected CompletableFuture<Boolean> hasTarget(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
                    @SuppressWarnings("deprecation") OfflinePlayer player = this.plugin.getServer().getOfflinePlayer(playerName);
//                    if (!player.hasPlayedBefore()) {
//                        this.cs.sendMessage(this.plugin.getMessages().getTargetNotPlayedBeforeError(player.getName()));
//                        return false;
//                    }
                    this.target = player;
                    return true;
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }

    public void sendUsageMessage() {
        String[] message = new String[]{this.plugin.getMessages().getCommandUsageHeader(), CommandUtils.getFancyVersion(this.command), this.plugin.getMessages().getCommandUsageFooter()};
        this.cs.sendMessage(message);
    }
}
