package com.backtobedrock.rewardslite.configurations;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.configurations.sections.ConfigurationData;
import com.backtobedrock.rewardslite.configurations.sections.ConfigurationGeneral;
import com.backtobedrock.rewardslite.configurations.sections.ConfigurationInterfaces;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Configurations {

    private final Rewardslite plugin;
    private final FileConfiguration config;

    //configurations
    private ConfigurationData dataConfiguration;
    private ConfigurationGeneral generalConfiguration;
    private ConfigurationInterfaces interfacesConfiguration;

    public Configurations(File configFile) {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.config = YamlConfiguration.loadConfiguration(configFile);
        this.getDataConfiguration();
        this.getInterfacesConfiguration();
    }

    public ConfigurationData getDataConfiguration() {
        if (this.dataConfiguration == null) {
            this.dataConfiguration = ConfigurationData.deserialize(this.config);
            if (this.dataConfiguration == null) {
                this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            }
        }
        return this.dataConfiguration;
    }

    public ConfigurationInterfaces getInterfacesConfiguration() {
        if (this.interfacesConfiguration == null) {
            this.interfacesConfiguration = ConfigurationInterfaces.deserialize(this.config);
            if (this.interfacesConfiguration == null) {
                this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            }
        }
        return this.interfacesConfiguration;
    }

    public ConfigurationGeneral getGeneralConfiguration() {
        if (this.generalConfiguration == null) {
            this.generalConfiguration = ConfigurationGeneral.deserialize(this.config);
        }
        return this.generalConfiguration;
    }
}
