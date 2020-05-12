package com.backtobedrock.LitePlaytimeRewards.commands;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class LitePlaytimeRewardsCommand {
    
    LitePlaytimeRewards plugin;
    CommandSender cs;
    Player sender;
    String[] args;
    
    public LitePlaytimeRewardsCommand(CommandSender cs, String[] args) {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        this.cs = cs;
        this.sender = cs instanceof Player ? (Player) cs : null;
        this.args = args;
    }
    
    public abstract void run();
    
    public boolean checkPermission(String permission) {
        if (!this.sender.hasPermission("liteplaytimerewards." + permission)) {
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
    
    public void sendUsageMessage(String usage) {
        this.cs.sendMessage("§bCommand usage:\n" + usage);
    }
}
