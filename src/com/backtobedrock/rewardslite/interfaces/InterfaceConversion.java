package com.backtobedrock.rewardslite.interfaces;

import com.backtobedrock.rewardslite.domain.CustomHolder;
import com.backtobedrock.rewardslite.domain.Icon;
import com.backtobedrock.rewardslite.domain.enumerations.ConversionType;
import com.backtobedrock.rewardslite.domain.enumerations.StorageType;
import com.backtobedrock.rewardslite.interfaces.clickActions.ClickActionCloseInterface;
import com.backtobedrock.rewardslite.interfaces.clickActions.ClickActionConfirmConversion;
import com.backtobedrock.rewardslite.interfaces.clickActions.ClickActionToggleConversionType;
import com.backtobedrock.rewardslite.utilities.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InterfaceConversion extends AbstractInterface {
    private boolean convertPlayerData;
    private boolean convertRewards;
    private boolean convertYamlToMysql;

    public InterfaceConversion() {
        super(new CustomHolder(21, "Convert Data"));
        this.initialize();
    }

    @Override
    protected void initialize() {
        super.initialize();
        this.setAccentColor(Arrays.asList(0, 4, 8, 9, 10, 16, 17, 21, 23, 28, 34, 36, 40, 44));
        this.fillInterface(Arrays.asList(11, 13, 15, 30, 32));
        this.setData();
    }

    @Override
    protected void setData() {
        this.updateRewardsConversion(false);
        this.updatePlayerDataConversion(false);
        this.updateYamlToMysqlConversion(false);
        this.updateConversionDeny();
        this.updateConversionConfirm();
    }

    private void updateRewardsConversion(boolean update) {
        ItemStack iconItem = ItemUtils.createItem(Material.BOOK, String.format("§bConvert Rewards %s", this.convertRewards ? "§a(enabled)" : "§c(disabled)"), Collections.singletonList("§dConvert the LPR rewards to RL."), 1, this.convertRewards);
        Icon icon = new Icon(iconItem, Collections.singletonList(new ClickActionToggleConversionType(this, ConversionType.REWARDS)));
        this.customHolder.setIcon(11, icon, update);
    }

    private void updatePlayerDataConversion(boolean update) {
        List<String> lore = new ArrayList<>(Arrays.asList("§dConvert the LPR player data to RL.", "", "§e§oRequires rewards conversion."));
        if (this.plugin.getConfigurations().getDataConfiguration().getStorageType() == StorageType.MYSQL) {
            lore.add("§6§oWill convert YAML §b§o-> §6§oMySQL");
        }
        ItemStack iconItem = ItemUtils.createItem(Material.DIAMOND_HELMET, String.format("§bConvert Player Data %s", this.convertPlayerData ? "§a(enabled)" : "§c(disabled)"), lore, 1, this.convertPlayerData);
        Icon icon = new Icon(iconItem, Collections.singletonList(new ClickActionToggleConversionType(this, ConversionType.PLAYERS)));
        this.customHolder.setIcon(13, icon, update);
    }

    private void updateYamlToMysqlConversion(boolean update) {
        ItemStack iconItem = ItemUtils.createItem(Material.DAYLIGHT_DETECTOR, String.format("§bConvert YAML To MySQL %s", this.convertYamlToMysql ? "§a(enabled)" : "§c(disabled)"), Collections.singletonList("§dConvert YAML player data to MySQL."), 1, this.convertYamlToMysql);
        Icon icon = new Icon(iconItem, Collections.singletonList(new ClickActionToggleConversionType(this, ConversionType.YAMLTOMYSQL)));
        this.customHolder.setIcon(15, icon, update);
    }

    private void updateConversionDeny() {
        ItemStack iconItem = ItemUtils.createItem(Material.BARRIER, "§cDeny Conversion", Collections.emptyList(), 1, this.convertPlayerData);
        Icon icon = new Icon(iconItem, Collections.singletonList(new ClickActionCloseInterface()));
        this.customHolder.setIcon(30, icon, false);
    }

    private void updateConversionConfirm() {
        ItemStack iconItem = ItemUtils.createItem(Material.EMERALD_BLOCK, "§aConfirm Conversion", Arrays.asList("§cConverted data will override current data.", "§dThis may take a while and lag the server.", "", "§e§oAre you sure you want to continue?"), 1, this.convertPlayerData);
        Icon icon = new Icon(iconItem, Arrays.asList(new ClickActionCloseInterface(), new ClickActionConfirmConversion(this)));
        this.customHolder.setIcon(32, icon, false);
    }

    public void togglePlayerDataConversion() {
        this.convertPlayerData = !this.convertPlayerData;
        if (this.convertPlayerData) {
            this.convertRewards = true;
            this.updateRewardsConversion(true);
            if (this.plugin.getConfigurations().getDataConfiguration().getStorageType() == StorageType.MYSQL) {
                this.convertYamlToMysql = true;
                this.updateYamlToMysqlConversion(true);
            }
        }
        this.updatePlayerDataConversion(true);
    }

    public void toggleRewardsConversion() {
        if (this.convertPlayerData) {
            return;
        }
        this.convertRewards = !this.convertRewards;
        this.updateRewardsConversion(true);
    }

    public void toggleYamlToMysqlConversion() {
        if (this.convertPlayerData && this.plugin.getConfigurations().getDataConfiguration().getStorageType() == StorageType.MYSQL) {
            return;
        }
        this.convertYamlToMysql = !this.convertYamlToMysql;
        this.updateYamlToMysqlConversion(true);
    }

    public boolean isConvertPlayerData() {
        return convertPlayerData;
    }

    public boolean isConvertRewards() {
        return convertRewards;
    }

    public boolean isConvertYamlToMysql() {
        return convertYamlToMysql;
    }
}
