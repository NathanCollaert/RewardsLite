package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.configs.Config;
import com.backtobedrock.LitePlaytimeRewards.configs.Messages;
import com.backtobedrock.LitePlaytimeRewards.configs.PlayerData;
import com.backtobedrock.LitePlaytimeRewards.eventHandlers.LitePlaytimeRewardsEventHandlers;
import com.backtobedrock.LitePlaytimeRewards.guis.GiveRewardGUI;
import com.backtobedrock.LitePlaytimeRewards.models.ConfigReward;
import com.backtobedrock.LitePlaytimeRewards.models.Reward;
import com.backtobedrock.LitePlaytimeRewards.utils.UpdateChecker;
import com.backtobedrock.LitePlaytimeRewards.models.GUIReward;
import com.backtobedrock.LitePlaytimeRewards.runnables.NotifyBossBar;
import com.backtobedrock.LitePlaytimeRewards.utils.Metrics;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import net.ess3.api.IEssentials;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class LitePlaytimeRewards extends JavaPlugin implements Listener {

    private boolean oldVersion = false;
    public IEssentials ess;
    private boolean legacy;

    private Config config;
    private Messages messages;
    private LitePlaytimeRewardsCommands commands;

    private final TreeMap<UUID, Integer> runnableCache = new TreeMap<>();
    private final TreeMap<UUID, PlayerData> playerCache = new TreeMap<>();
    private final TreeMap<UUID, GiveRewardGUI> GUICache = new TreeMap<>();
    private final TreeMap<UUID, GUIReward> isGivingReward = new TreeMap<>();

    @Override
    public void onEnable() {
        //register Reward for serialization
        ConfigurationSerialization.registerClass(Reward.class);

        this.initialize();

        //register eventhandler class
        getServer().getPluginManager().registerEvents(new LitePlaytimeRewardsEventHandlers(), this);

        //bStats
        if (this.config.isUsebStats()) {
            Metrics metrics = new Metrics(this, 7380);
            metrics.addCustomChart(new Metrics.SimplePie("reward_count", () -> Integer.toString(this.getLPRConfig().getRewards().size())));
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        //save players
        this.playerCache.entrySet().forEach(e -> e.getValue().saveConfig());

        //remove all runnables from plugin
        Bukkit.getScheduler().cancelTasks(this);

        super.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        return this.commands.onCommand(cs, cmnd, alias, args);
    }

    public void initialize() {
        this.createFiles();

        String a = this.getServer().getClass().getPackage().getName();
        if (a.substring(a.lastIndexOf(".") + 1).equalsIgnoreCase("v1_12_R1")) {
            this.legacy = true;
            this.getLogger().severe("Running legacy version, disabling some features.");
        }

        this.checkDependencies();

        //initialize commands.
        this.commands = new LitePlaytimeRewardsCommands();
    }

    private void createFiles() {
        //get config.yml and make if not exists
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            this.getLogger().info("Creating config.yml file.");
            this.saveResource("config.yml", false);
        }

        //make userdata folder if not exist
        File udFile = new File(this.getDataFolder() + "/userdata");
        if (udFile.mkdirs()) {
            this.getLogger().info("Creating userdata folder.");
        }

        //get messages.yml and make if not exists
        File messagesFile = new File(this.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            this.getLogger().info("Creating messages.yml file.");
            this.saveResource("messages.yml", false);
        }

        //initialize config, messages and command classes
        this.config = new Config(configFile);
        this.messages = new Messages(messagesFile);
    }

    private void checkDependencies() {
        //check if essentials is installed
        this.ess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");
        if (this.ess == null) {
            this.getLogger().info("Essentials not found, AFK time won't be counted.");
        }
    }

    public int giveReward(Reward reward, OfflinePlayer plyr, boolean broadcast, int amount) {
        String message = "";
        boolean notified = false;
        int pending = 0;
        if (plyr.isOnline()) {
            Player player = (Player) plyr;
            //give reward this amount of times
            for (int i = 0; i < amount + reward.getAmountPending(); i++) {

                //check if enough free invent space
                int emptySlots = 0;
                for (ItemStack it : player.getInventory().getStorageContents()) {
                    if (it == null) {
                        emptySlots++;
                    }
                }

                //check if in world that doesn't allow for rewards and free invent
                if (this.config.getDisableGettingRewardsInWorlds().contains(player.getWorld().getName().toLowerCase())) {
                    message = this.getMessages().getPendingNotificationWrongWorld(plyr.getName(), reward.getcReward().getDisplayName(), player.getWorld().getName());
                    pending++;
                } else if (emptySlots < reward.getcReward().getSlotsNeeded()) {
                    message = this.getMessages().getPendingNotificationNotEnoughInventory(plyr.getName(), reward.getcReward().getDisplayName(), reward.getcReward().getSlotsNeeded());
                    pending++;
                } else {
                    reward.getcReward().getCommands().forEach(j -> {
                        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), j.replaceAll("%player%", plyr.getName()));
                    });

                    reward.setAmountRedeemed(reward.getAmountRedeemed() + 1);

                    if (!notified) {
                        //notify user and broadcast on getting reward
                        this.notifyUsers(reward.getcReward(), player, broadcast);
                        notified = true;
                    }
                }
            }

            if (!message.equals("") && amount > 0) {
                switch (reward.getcReward().getNotificationType().toLowerCase()) {
                    case "chat":
                        player.sendMessage(message);
                        break;
                    case "bossbar":
                        Collection<Player> players = new ArrayList<>();
                        players.add(player);
                        new NotifyBossBar(players, message, BarColor.YELLOW).runTaskTimer(this, 0, 20);
                        break;
                    case "actionbar":
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(message).create());
                        break;
                }
            }
        } else {
            pending = amount + reward.getAmountPending();
        }

        return pending;
    }

    private void notifyUsers(ConfigReward reward, Player plyr, boolean broadcast) {
        String notification = reward.getNotification().replaceAll("%player%", plyr.getName());
        String broadcastNotification = reward.getBroadcastNotification().replaceAll("%player%", plyr.getName());

        Collection<Player> players = new ArrayList<>();

        switch (reward.getNotificationType().toLowerCase()) {
            case "chat":
                if (!broadcastNotification.isEmpty() && broadcast) {
                    Bukkit.broadcastMessage(broadcastNotification);
                }
                if (!notification.isEmpty()) {
                    plyr.sendMessage(notification);
                }
                break;
            case "bossbar":
                if (!broadcastNotification.isEmpty() && broadcast) {
                    players = this.getServer().getOnlinePlayers().stream().map(e -> (Player) e).collect(Collectors.toList());
                    if (!notification.isEmpty()) {
                        players.remove(plyr);
                    }
                    new NotifyBossBar(players, broadcastNotification, BarColor.BLUE).runTaskTimer(this, 0, 20);
                }
                if (!notification.isEmpty()) {
                    players.add(plyr);
                    new NotifyBossBar(players, notification, BarColor.GREEN).runTaskTimer(this, 0, 20);
                }
                break;
            case "actionbar":
                if (!broadcastNotification.isEmpty() && broadcast) {
                    players = this.getServer().getOnlinePlayers().stream().map(e -> (Player) e).collect(Collectors.toList());
                    if (!notification.isEmpty()) {
                        players.remove(plyr);
                    }
                    players.stream().forEach(e -> {
                        ((Player) e).spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(broadcastNotification).create());
                    });
                }
                if (!notification.isEmpty()) {
                    plyr.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(notification).create());
                }
                break;
        }
    }

    public Config getLPRConfig() {
        return this.config;
    }

    public Messages getMessages() {
        return messages;
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
        if (this.getLPRConfig().isUpdateChecker()) {
            new UpdateChecker(71784).getVersion(version -> {
                this.oldVersion = !this.getDescription().getVersion().equalsIgnoreCase(version);
            });
        }
    }

    public boolean isLegacy() {
        return legacy;
    }
}
