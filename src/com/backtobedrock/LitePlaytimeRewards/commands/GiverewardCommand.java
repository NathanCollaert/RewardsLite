package com.backtobedrock.LitePlaytimeRewards.commands;

import com.backtobedrock.LitePlaytimeRewards.enums.Command;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.configs.PlayerData;
import com.backtobedrock.LitePlaytimeRewards.guis.GiveRewardGUI;
import com.backtobedrock.LitePlaytimeRewards.models.ConfigReward;
import com.backtobedrock.LitePlaytimeRewards.models.Reward;
import java.util.TreeMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class GiverewardCommand extends Commands {

    public GiverewardCommand(CommandSender cs, String[] args) {
        super(cs, args);
    }

    @Override
    public void run() {
        switch (this.args.length) {
            case 0:
                //check for permission
                if (this.checkIfPlayer() && this.checkPermission("givereward")) {
                    //check if rewards available
                    TreeMap<String, ConfigReward> giveRewards = this.plugin.getRewards().getAll();
                    if (giveRewards.isEmpty()) {
                        this.cs.sendMessage(this.plugin.getMessages().getNoRewardsConfigured());
                        break;
                    }

                    //Create GUI, add to list and open
                    GiveRewardGUI giveGUI = new GiveRewardGUI(giveRewards);
                    this.plugin.getGUICache().put(this.sender.getUniqueId(), giveGUI);
                    this.sender.openInventory(giveGUI.getInventory());
                }
                break;
            case 2:
                //check for permission
                if (this.checkPermission("givereward")) {
                    if (GiverewardCommand.giveRewardCommand(this.cs, this.args[0], this.args[1], true, 1)) {
                        this.cs.sendMessage(this.plugin.getMessages().getRewardGiven(this.args[1], this.args[0]));
                    }
                }
                break;
            case 3:
                if (this.checkPermission("givereward") && this.checkNumber()) {
                    if (GiverewardCommand.giveRewardCommand(cs, args[0], args[1], true, Integer.parseInt(args[2]))) {
                        cs.sendMessage(this.plugin.getMessages().getRewardGiven(args[1], args[0]));
                    }
                }
                break;
            case 4:
                //check for permission
                if (this.checkPermission("givereward") && this.checkNumber()) {
                    //check if boolean
                    if (!this.args[3].equalsIgnoreCase("true") && !this.args[3].equalsIgnoreCase("false")) {
                        this.cs.sendMessage(this.plugin.getMessages().getNotABoolean());
                        break;
                    }

                    if (GiverewardCommand.giveRewardCommand(this.cs, this.args[0], this.args[1], Boolean.getBoolean(this.args[3]), Integer.parseInt(this.args[2]))) {
                        this.cs.sendMessage(this.plugin.getMessages().getRewardGiven(this.args[1], this.args[0]));
                    }
                }
                break;
            default:
                this.sendUsageMessage(Command.GIVEREWARD);
                break;
        }
    }

    private boolean checkNumber() {
        try {
            Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            cs.sendMessage(this.plugin.getMessages().getNotANumber());
            return false;
        }
        return true;
    }

    public static boolean giveRewardCommand(CommandSender cs, String rewardName, String playerName, boolean broadcast, int amount) {
        LitePlaytimeRewards plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);

        //check if reward exists
        if (!plugin.getRewards().doesRewardExist(rewardName)) {
            cs.sendMessage(plugin.getMessages().getNoSuchReward(rewardName));
            return false;
        }

        //check if amount is bigger then 1
        if (amount < 1) {
            cs.sendMessage(plugin.getMessages().getNotANumber());
            return false;
        }

        //check if player has data on server
        OfflinePlayer plyr = Bukkit.getOfflinePlayer(playerName);
        if (!PlayerData.doesPlayerDataExists(plyr)) {
            cs.sendMessage(plugin.getMessages().getNoData(plyr.getName()));
            return false;
        }

        PlayerData crud = plyr.isOnline() ? plugin.getPlayerCache().get(plyr.getUniqueId()) : new PlayerData(plyr);

        //get reward
        Reward reward = crud.getRewards().get(rewardName.toLowerCase());

        if (reward != null) {
            //give reward and set pending
            reward.setAmountPending(reward.giveReward(plyr, broadcast, amount));
            crud.saveConfig();
        }

        return true;
    }

}
