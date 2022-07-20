package com.backtobedrock.rewardslite.interfaces.clickActions;

import com.backtobedrock.rewardslite.interfaces.InterfaceConversion;
import com.backtobedrock.rewardslite.utilities.ConversionUtils;
import org.bukkit.entity.Player;

public class ClickActionConfirmConversion extends AbstractClickAction {
    private final InterfaceConversion interfaceConversion;

    public ClickActionConfirmConversion(InterfaceConversion interfaceConversion) {
        this.interfaceConversion = interfaceConversion;
    }

    @Override
    public void execute(Player player) {
        this.plugin.getServer().getOnlinePlayers().forEach(p -> this.plugin.getPlayerRepository().getByPlayerSync(p).getPlaytimeRunnable().stop());
        if (this.interfaceConversion.isConvertRewards()) {
            ConversionUtils.convertLitePlaytimeRewardsRewards(player);
        }
        if (this.interfaceConversion.isConvertPlayerData()) {
            ConversionUtils.convertLitePlaytimeRewardsPlayerData(player);
        }
        if (this.interfaceConversion.isConvertYamlToMysql()) {
            ConversionUtils.convertYamlToMysqlPlayerData(player, this.interfaceConversion.isConvertPlayerData());
        }
        this.plugin.initialize();
        player.sendMessage("§aSuccessfully reloaded plugin with converted data.");
    }
}
