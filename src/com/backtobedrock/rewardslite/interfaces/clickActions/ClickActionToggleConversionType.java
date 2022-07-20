package com.backtobedrock.rewardslite.interfaces.clickActions;

import com.backtobedrock.rewardslite.domain.enumerations.ConversionType;
import com.backtobedrock.rewardslite.interfaces.InterfaceConversion;
import org.bukkit.entity.Player;

public class ClickActionToggleConversionType extends AbstractClickAction {
    private final ConversionType conversionType;
    private final InterfaceConversion conversionInterface;

    public ClickActionToggleConversionType(InterfaceConversion conversionInterface, ConversionType conversionType) {
        this.conversionInterface = conversionInterface;
        this.conversionType = conversionType;
    }

    @Override
    public void execute(Player player) {
        switch (this.conversionType) {
            case PLAYERS:
                this.conversionInterface.togglePlayerDataConversion();
                break;
            case REWARDS:
                this.conversionInterface.toggleRewardsConversion();
                break;
            case YAMLTOMYSQL:
                this.conversionInterface.toggleYamlToMysqlConversion();
                break;
        }
    }
}
