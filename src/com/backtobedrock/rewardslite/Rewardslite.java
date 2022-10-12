package com.backtobedrock.rewardslite;

import com.backtobedrock.rewardslite.commands.Commands;
import com.backtobedrock.rewardslite.configurations.Configurations;
import com.backtobedrock.rewardslite.configurations.MessagesConfiguration;
import com.backtobedrock.rewardslite.domain.enumerations.StorageType;
import com.backtobedrock.rewardslite.domain.placholderAPI.PlaceholdersRewardsLite;
import com.backtobedrock.rewardslite.interfaces.AbstractInterface;
import com.backtobedrock.rewardslite.interfaces.InterfaceRewards;
import com.backtobedrock.rewardslite.listeners.AbstractEventListener;
import com.backtobedrock.rewardslite.listeners.ListenerCustomInventory;
import com.backtobedrock.rewardslite.listeners.ListenerPlayerJoin;
import com.backtobedrock.rewardslite.listeners.ListenerPlayerQuit;
import com.backtobedrock.rewardslite.repositories.PlayerRepository;
import com.backtobedrock.rewardslite.repositories.RewardRepository;
import com.backtobedrock.rewardslite.runnables.UpdateChecker;
import com.tchristofferson.configupdater.ConfigUpdater;
import net.ess3.api.IEssentials;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class Rewardslite extends JavaPlugin {

    //various
    private final Map<Class<?>, AbstractEventListener> activeEventListeners = new HashMap<>();
    private final Map<UUID, AbstractInterface> openInterfaces = new HashMap<>();
    private UpdateChecker updateChecker;

    public static boolean PAPI_ENABLED = false;

    //configurations
    private Commands commands;
    private Configurations configurations;
    private MessagesConfiguration messagesConfiguration;

    //repositories
    private PlayerRepository playerRepository;
    private RewardRepository rewardsRepository;

    //dependencies
    private IEssentials essentials;

    @Override
    public void onEnable() {
        this.initialize();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        this.getServer().getOnlinePlayers().forEach(p -> this.playerRepository.updatePlayerData(this.playerRepository.getByPlayerSync(p)));
        super.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        return this.commands.onCommand(cs, cmnd, alias, args);
    }

    public void initialize() {
        this.initializeUpdateChecker();
        this.initializeMetrics();
        this.initializeDependencies();
        this.initializeConfigurations();
        this.initializeDatabase(false);
        this.initializeRepositories();
        this.registerListeners();
    }

    private void initializeUpdateChecker() {
        if (this.updateChecker == null) {
            this.updateChecker = new UpdateChecker();
            this.updateChecker.start();
        }
    }

    private void initializeMetrics() {
        //bstats metrics
        Metrics metrics = new Metrics(this, 7380);
        metrics.addCustomChart(new SimplePie("reward_count", () -> Integer.toString(this.getRewardsRepository().getAll().size())));
    }

    private void initializeDependencies() {
        //PAPI
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholdersRewardsLite().register();
            PAPI_ENABLED = true;
        }

        //EssentialsX
        this.essentials = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
    }

    private void initializeConfigurations() {
        //get config.yml and make if none existent
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            this.getLogger().log(Level.INFO, "Creating {0}.", configFile.getAbsolutePath());
            this.saveResource("config.yml", false);
        }

        //get messages.yml and make if none existent
        File messagesFile = new File(this.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            this.getLogger().log(Level.INFO, "Creating {0}.", messagesFile.getAbsolutePath());
            this.saveResource("messages.yml", false);
        }

        //initialize configurations
        try {
            ConfigUpdater.update(this, "config.yml", configFile, Collections.emptyList());
            configFile = new File(this.getDataFolder(), "config.yml");
            ConfigUpdater.update(this, "messages.yml", messagesFile, Collections.emptyList());
            messagesFile = new File(this.getDataFolder(), "messages.yml");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //initialize config, messages and commands
        this.configurations = new Configurations(configFile);
        this.messagesConfiguration = new MessagesConfiguration(messagesFile);
        this.commands = new Commands();
    }

    public void initializeDatabase(boolean bypassStorageType) {
        if (!bypassStorageType && this.getConfigurations().getDataConfiguration().getStorageType() != StorageType.MYSQL) {
            return;
        }

        String setup = "";
        try (InputStream in = getClassLoader().getResourceAsStream("database-setup.sql")) {
            setup = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining());
        } catch (IOException | NullPointerException e) {
            getLogger().log(Level.SEVERE, "Could not read db setup file.", e);
            e.printStackTrace();
        }
        String[] queries = setup.split(";");
        for (String query : queries) {
            if (query.isEmpty()) {
                return;
            }
            try (Connection conn = this.getConfigurations().getDataConfiguration().getDatabase().getDataSource().getConnection(); PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //PATCHES
//        Arrays.asList().forEach(Patch::executePatch);
    }

    private void initializeRepositories() {
        if (this.rewardsRepository == null) {
            this.rewardsRepository = new RewardRepository();
        } else {
            this.rewardsRepository.onReload();
        }

        if (this.playerRepository == null && this.isEnabled()) {
            this.playerRepository = new PlayerRepository();
        } else if (this.playerRepository != null) {
            this.playerRepository.onReload();
        }
    }

    private void registerListeners() {
        if (!this.isEnabled()) {
            return;
        }

        Arrays.asList(
                new ListenerCustomInventory(),
                new ListenerPlayerJoin(),
                new ListenerPlayerQuit()
        ).forEach(e ->
                {
                    if (this.activeEventListeners.containsKey(e.getClass())) {
                        AbstractEventListener listener = this.activeEventListeners.get(e.getClass());
                        if (!listener.isEnabled()) {
                            HandlerList.unregisterAll(listener);
                            this.activeEventListeners.remove(listener.getClass());
                        }
                    } else if (e.isEnabled()) {
                        getServer().getPluginManager().registerEvents(e, this);
                        this.activeEventListeners.put(e.getClass(), e);
                    }
                }
        );
    }

    public Configurations getConfigurations() {
        return configurations;
    }

    public MessagesConfiguration getMessages() {
        return messagesConfiguration;
    }

    public PlayerRepository getPlayerRepository() {
        return playerRepository;
    }

    public IEssentials getEssentials() {
        return essentials;
    }

    public void addToInterfaces(Player player, AbstractInterface iface) {
        this.openInterfaces.put(player.getUniqueId(), iface);
    }

    public void removeFromInterfaces(Player player) {
        AbstractInterface gui = this.openInterfaces.remove(player.getUniqueId());
        if (gui == null) {
            return;
        }

        if (gui instanceof InterfaceRewards) {
            ((InterfaceRewards) gui).unregisterObservers();
        }
    }

    public RewardRepository getRewardsRepository() {
        return rewardsRepository;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}
