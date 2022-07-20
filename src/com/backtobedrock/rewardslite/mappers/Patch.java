package com.backtobedrock.rewardslite.mappers;

import com.backtobedrock.rewardslite.Rewardslite;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public abstract class Patch extends AbstractMapper {
    private final Rewardslite plugin;
    protected boolean success = false;

    public Patch() {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
    }

    protected abstract boolean hasBeenApplied();

    protected abstract void applyPatch();

    protected void execute(String sql) {
        try (Connection connection = this.database.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.execute();
            this.success = true;
        } catch (SQLException e) {
            this.success = false;
            e.printStackTrace();
        }
    }

    public void executePatch() {
        if (!this.hasBeenApplied()) {
            this.applyPatch();
            if (!this.success) {
                this.plugin.getLogger().log(Level.SEVERE, String.format("%s failed, plugin will disable itself.", this.getClass().getSimpleName()));
                this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
            }
        }
    }

    protected Boolean doesColumnExist(String tableName, String columnName) {
        String sql = "SELECT COUNT(1) as c FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME=? AND COLUMN_NAME=?;";
        try (Connection connection = this.database.getDataSource().getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, tableName);
            preparedStatement.setString(2, columnName);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() && resultSet.getInt("c") > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}