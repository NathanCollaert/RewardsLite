package com.backtobedrock.rewardslite.configurations.sections;

import com.backtobedrock.rewardslite.domain.Database;
import com.backtobedrock.rewardslite.domain.enumerations.StorageType;
import com.backtobedrock.rewardslite.utilities.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;

public class ConfigurationData {
    private final StorageType storageType;
    private final Database database;

    public ConfigurationData(StorageType storageType, Database database) {
        this.storageType = storageType;
        this.database = database;
    }

    public static ConfigurationData deserialize(ConfigurationSection section) {
        //configurations
        StorageType cStorageType = ConfigUtils.getStorageType("storageType", section.getString("storageType", "YAML"));
        ConfigurationSection connectionSection = section.getConfigurationSection("connection");
        Database cConnection = connectionSection != null ? Database.deserialize(connectionSection) : null;

        if (cStorageType == StorageType.MYSQL && cConnection == null) {
            return null;
        }

        return new ConfigurationData(cStorageType, cConnection);
    }

    public StorageType getStorageType() {
        return storageType;
    }

    public Database getDatabase() {
        return database;
    }
}
