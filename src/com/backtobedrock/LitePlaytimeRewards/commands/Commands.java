package com.backtobedrock.LitePlaytimeRewards.commands;

import com.backtobedrock.LitePlaytimeRewards.enums.Command;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Commands {

    LitePlaytimeRewards plugin;
    CommandSender cs;
    Player sender;
    String[] args;

    public Commands(CommandSender cs, String[] args) {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        this.cs = cs;
        this.sender = cs instanceof Player ? (Player) cs : null;
        this.args = args;
    }

    public abstract void run();

    public boolean checkPermission(String permission) {
        if (!this.cs.hasPermission("liteplaytimerewards." + permission)) {
            this.cs.sendMessage(this.plugin.getMessages().getNoPermission());
            return false;
        }
        return true;
    }

    public boolean checkIfPlayer() {
        if (this.sender == null) {
            this.cs.sendMessage(this.plugin.getMessages().getNeedToBeOnline());
            return false;
        }
        return true;
    }

    public void sendUsageMessage(Command command) {
        this.cs.sendMessage(new String[]{"§8§m--------------§6 Command §fUsage §8§m--------------", command.getFancyVersion(), "§8§m------------------------------------------"});
    }
}
