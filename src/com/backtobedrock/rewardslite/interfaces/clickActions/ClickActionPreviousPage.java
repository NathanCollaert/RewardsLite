package com.backtobedrock.rewardslite.interfaces.clickActions;

import com.backtobedrock.rewardslite.interfaces.AbstractPaginatedInterface;
import org.bukkit.entity.Player;

public class ClickActionPreviousPage extends AbstractClickAction {
    private final AbstractPaginatedInterface paginatedGui;

    public ClickActionPreviousPage(AbstractPaginatedInterface paginatedGui) {
        this.paginatedGui = paginatedGui;
    }

    @Override
    public void execute(Player player) {
        this.paginatedGui.previousPage();
    }
}
