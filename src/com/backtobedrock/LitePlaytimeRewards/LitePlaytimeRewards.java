package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.configs.*;
import com.backtobedrock.LitePlaytimeRewards.eventHandlers.LitePlaytimeRewardsEventHandlers;
import com.backtobedrock.LitePlaytimeRewards.guis.GiveRewardGUI;
import com.backtobedrock.LitePlaytimeRewards.models.GUIReward;
import com.backtobedrock.LitePlaytimeRewards.models.Reward;
import com.backtobedrock.LitePlaytimeRewards.utils.ConfigUpdater;
import com.backtobedrock.LitePlaytimeRewards.utils.ConfigUtils;
import com.backtobedrock.LitePlaytimeRewards.utils.Metrics;
import com.backtobedrock.LitePlaytimeRewards.utils.UpdateChecker;
import me.clip.placeholderapi.PlaceholderAPI;
import net.ess3.api.IEssentials;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Collections;
import java.util.TreeMap;
import java.util.UUID;

public class LitePlaytimeRewards extends JavaPlugin implements Listener {

    private final TreeMap<UUID, Integer> runnableCache = new TreeMap<>();
    private final TreeMap<UUID, PlayerData> playerCache = new TreeMap<>();
    private final TreeMap<UUID, GiveRewardGUI> GUICache = new TreeMap<>();
    private final TreeMap<UUID, GUIReward> isGivingReward = new TreeMap<>();
    public IEssentials ess;
    private boolean oldVersion = false;
    private boolean legacy;
    //configs
    private Config config;
    private Messages messages;
    private Rewards rewards;
    private ServerData serverData;
    private LitePlaytimeRewardsCommands commands;
    private static boolean papiEnabled = false;

    @Override
    public void onEnable() {
        //register Reward for serialization
        ConfigurationSerialization.registerClass(Reward.class);

        // Small check to make sure that PlaceholderAPI is installed
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info("Placeholder API found, placeholders supported.");
            LitePlaytimeRewards.papiEnabled = true;
        } else {
            this.getLogger().info("PlaceholderAPI not found.");
        }

        //initialize plugin
        this.initialize();

        //register eventhandler class
        getServer().getPluginManager().registerEvents(new LitePlaytimeRewardsEventHandlers(), this);

        //bStats
        Metrics metrics = new Metrics(this, 7380);
        metrics.addCustomChart(new Metrics.SimplePie("reward_count", () -> Integer.toString(this.rewards.getAll().size())));

        super.onEnable();
    }

    /**
     * Resolves PAPI placeholders.
     *
     * @param player The player object.
     * @param text The text which may contain placeholders.
     * @return The final text.
     */
    public static String replacePlaceholders(Player player, String text) {
        if(papiEnabled) {
            return PlaceholderAPI.setPlaceholders(player, text);
        }
        return text;
    }

    @Override
    public void onDisable() {
        //save players
        this.playerCache.forEach((key, value) -> value.saveConfig());

        //remove all runnables from plugin
        Bukkit.getScheduler().cancelTasks(this);

        super.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        return this.commands.onCommand(cs, cmnd, alias, args);
    }

    public void initialize() {
        //create/load all files
        this.createFiles();

        //check legacy
        String a = this.getServer().getClass().getPackage().getName();
        if (a.substring(a.lastIndexOf(".") + 1).equalsIgnoreCase("v1_12_R1")) {
            this.legacy = true;
            this.getLogger().severe("Running legacy version, disabling some features.");
        }

        //check if dependencies are loaded
        this.checkDependencies();

        //initialize commands
        this.commands = new LitePlaytimeRewardsCommands();
    }

    private void createFiles() {
        //make userdata folder if not exist
        File udFile = new File(this.getDataFolder() + "/userdata");
        if (udFile.mkdirs()) {
            this.getLogger().info("Creating userdata folder.");
        }

        //get config.yml and make if not exists
        File configFile = new File(this.getDataFolder(), "config.yml");
        boolean wasOld = false;
        if (!configFile.exists()) {
            this.getLogger().info("Creating config.yml file.");
            this.saveResource("config.yml", false);
        } else {
            wasOld = true;
        }

        //get messages.yml and make if not exists
        File messagesFile = new File(this.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            this.getLogger().info("Creating messages.yml file.");
            this.saveResource("messages.yml", false);
        }

        //get rewards.yml and make if not exists
        File rewardsFile = new File(this.getDataFolder(), "rewards.yml");
        boolean wasNew = false;
        if (!rewardsFile.exists()) {
            this.getLogger().info("Creating rewards.yml file.");
            this.saveResource("rewards.yml", false);
            wasNew = true;
        }

//        //get server.yml and make if not exists
//        File serverFile = new File(this.getDataFolder(), "server.yml");
//        if (!serverFile.exists()) {
//            this.getLogger().info("Creating server.yml file.");
//            this.saveResource("server.yml", false);
//        }

        //initialize configs
        this.config = new Config(configFile);
        this.messages = new Messages(messagesFile);
        this.rewards = new Rewards(rewardsFile);
//        this.serverData = new ServerData(serverFile);

        //Update config file if old
        if (wasOld) {
            if (wasNew) {
                this.rewards.getAll().clear();
            }
            //check for old rewards
            ConfigUtils.convertOldRewards();
            ConfigUpdater.update(this, "config.yml", configFile, Collections.emptyList());
            configFile = new File(this.getDataFolder(), "config.yml");
            this.config = new Config(configFile);
        }
    }

    private void checkDependencies() {
        //check if essentials is installed
        this.ess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (this.ess == null) {
            this.getLogger().info("Essentials not found, AFK time won't be counted.");
        }
    }

    public Config getLPRConfig() {
        return this.config;
    }

    public Messages getMessages() {
        return messages;
    }

    public Rewards getRewards() {
        return this.rewards;
    }

    public boolean isOldVersion() {
        return oldVersion;
    }

    public TreeMap<UUID, Integer> getRunnableCache() {
        return this.runnableCache;
    }

    public TreeMap<UUID, PlayerData> getPlayerCache() {
        return this.playerCache;
    }

    public TreeMap<UUID, GiveRewardGUI> getGUICache() {
        return this.GUICache;
    }

    public TreeMap<UUID, GUIReward> getIsGiving() {
        return this.isGivingReward;
    }

    public void checkForOldVersion() {
        new UpdateChecker(71784).getVersion(version -> this.oldVersion = !this.getDescription().getVersion().equalsIgnoreCase(version));
    }

    public boolean isLegacy() {
        return legacy;
    }
}
