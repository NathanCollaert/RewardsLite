package com.backtobedrock.rewardslite.interfaces.clickActions;

import com.backtobedrock.rewardslite.utilities.PlayerUtils;
import org.bukkit.entity.Player;

public class ClickActionCloseInterface extends AbstractClickAction {
    @Override
    public void execute(Player player) {
        PlayerUtils.closeInventory(player);
    }
}
