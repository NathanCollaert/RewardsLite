package com.backtobedrock.LitePlaytimeRewards.guis.clickActions;

import com.backtobedrock.LitePlaytimeRewards.guis.PagedGUI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class NextPageClickAction extends ClickAction {

    private final PagedGUI gui;

    public NextPageClickAction(PagedGUI gui) {
        this.gui = gui;
    }

    @Override
    public void execute(Player player, ClickType type) {
        this.gui.nextPage();
        this.gui.initialize();
        Bukkit.getScheduler().runTask(this.plugin, () -> player.openInventory(gui.getInventory()));
    }

}
