package com.backtobedrock.rewardslite.mappers;

import com.backtobedrock.rewardslite.Rewardslite;
import com.backtobedrock.rewardslite.domain.Database;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class AbstractMapper {
    protected final Rewardslite plugin;
    protected final Database database;

    public AbstractMapper() {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.database = this.plugin.getConfigurations().getDataConfiguration().getDatabase();
    }
}
