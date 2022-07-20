package com.backtobedrock.rewardslite.interfaces.clickActions;

import com.backtobedrock.rewardslite.interfaces.AbstractPaginatedInterface;
import org.bukkit.entity.Player;

public class ClickActionNextPage extends AbstractClickAction {
    private final AbstractPaginatedInterface paginatedGui;

    public ClickActionNextPage(AbstractPaginatedInterface paginatedGui) {
        this.paginatedGui = paginatedGui;
    }

    @Override
    public void execute(Player player) {
        this.paginatedGui.nextPage();
    }
}
