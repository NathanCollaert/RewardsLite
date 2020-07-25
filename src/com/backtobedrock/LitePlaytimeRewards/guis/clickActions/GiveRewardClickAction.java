package com.backtobedrock.LitePlaytimeRewards.guis.clickActions;

import com.backtobedrock.LitePlaytimeRewards.guis.GUI;
import com.backtobedrock.LitePlaytimeRewards.guis.PagedGUI;
import com.backtobedrock.LitePlaytimeRewards.models.GUIReward;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class GiveRewardClickAction extends AdvancedClickAction {

    private final PagedGUI gui;
    private final GUIReward reward;

    public GiveRewardClickAction(PagedGUI gui, GUIReward reward) {
        this.gui = gui;
        this.reward = reward;
    }

    @Override
    public void execute(Player player, ClickType type) {
        switch (type) {
            //if right click and shift, decrease amount
            case SHIFT_RIGHT:
                this.reward.decreaseAmount();
                this.updateInventory(player);
                break;

            //if left click and shift, increase amount
            case SHIFT_LEFT:
                this.reward.increaseAmount();
                this.updateInventory(player);
                break;

            //if right click, switch broadcast
            case RIGHT:
                this.reward.toggleBroadcast();
                this.updateInventory(player);
                break;

            //if left click, give reward
            case LEFT:
                this.plugin.getIsGiving().put(player.getUniqueId(), this.reward);
                Bukkit.getScheduler().runTask(this.plugin, () -> player.closeInventory());
                player.spigot().sendMessage(new ComponentBuilder("Who do you want to give the reward to?").color(ChatColor.AQUA).create());
                player.spigot().sendMessage(new ComponentBuilder("Please Send the playername in chat (").color(ChatColor.AQUA).append("!cancel").color(ChatColor.DARK_AQUA).append(" to cancel).").color(ChatColor.AQUA).create());
                break;
        }
    }
    
    public void updateInventory(Player player){
        this.gui.initialize();
        Bukkit.getScheduler().runTask(this.plugin, () -> player.openInventory(this.gui.getInventory()));
    }
}
