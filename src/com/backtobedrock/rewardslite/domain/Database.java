package com.backtobedrock.rewardslite.domain;

import com.backtobedrock.rewardslite.Rewardslite;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.logging.Level;

public class Database {
    private final Rewardslite plugin;
    private final String hostname;
    private final String port;
    private final String databaseName;
    private final String username;
    private final String password;

    private HikariDataSource dataSource;

    public Database(String hostname, String port, String databaseName, String username, String password) {
        this.plugin = JavaPlugin.getPlugin(Rewardslite.class);
        this.hostname = hostname;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
    }

    public static Database deserialize(ConfigurationSection section) {
        String cHostname = section.getString("hostname", "localhost");
        String cPort = section.getString("port", "3306");
        String cDatabase = section.getString("database", "minecraft");
        String cUsername = section.getString("username", "root");
        String cPassword = section.getString("password", "root");
        return new Database(cHostname, cPort, cDatabase, cUsername, cPassword);
    }

    public String getHostname() {
        return hostname;
    }

    public String getPort() {
        return port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public HikariDataSource getDataSource() {
        if (this.dataSource == null) {
            try {
                this.dataSource = new HikariDataSource();
                this.dataSource.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s", this.getHostname(), this.getPort(), this.getDatabaseName()));
                this.dataSource.setUsername(this.getUsername());
                this.dataSource.setPassword(this.getPassword());
                this.dataSource.addDataSourceProperty("autoReconnect", "true");
                this.dataSource.addDataSourceProperty("useSSL", "false");
                this.dataSource.addDataSourceProperty("cachePrepStmts", "true");
                this.dataSource.addDataSourceProperty("prepStmtCacheSize", "250");
                this.dataSource.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                this.dataSource.addDataSourceProperty("useServerPrepStmts", "true");
            } catch (Exception e) {
                this.plugin.getLogger().log(Level.SEVERE, String.format("Database: cannot connect to %s with username: %s and password: %s.", String.format("jdbc:mysql://%s:%s/%s", this.getHostname(), this.getPort(), this.getDatabaseName()), this.getUsername(), String.join("", Collections.nCopies(this.getPassword().length(), "*"))));
            }
        }
        return this.dataSource;
    }
}
