package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.eventHandlers.LitePlaytimeRewardsEventHandlers;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.ConfigReward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.Reward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RewardsGUI;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.UpdateChecker;
import com.backtobedrock.LitePlaytimeRewards.runnables.NotifyBossBar;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import net.ess3.api.IEssentials;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bstats.bukkit.Metrics;
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

    private static LitePlaytimeRewards plugin;

    private boolean oldVersion = false;
    public IEssentials ess;

    private LitePlaytimeRewardsConfig config;
    private LitePlaytimeRewardsMessages messages;
    private LitePlaytimeRewardsCommands commands;

    private TreeMap<UUID, Integer> runnableCache;
    private TreeMap<UUID, LitePlaytimeRewardsCRUD> CRUDCache;
    private TreeMap<UUID, RewardsGUI> GUICache;

    @Override
    public void onEnable() {
        plugin = this;

        //initialize maps
        this.runnableCache = new TreeMap<>();
        this.CRUDCache = new TreeMap<>();
        this.GUICache = new TreeMap<>();

        //register Reward for serialization
        ConfigurationSerialization.registerClass(Reward.class);

        //save and get config
        this.saveDefaultConfig();

        //make userdata folder if not exist
        File dir = new File(this.getDataFolder() + "/userdata");
        dir.mkdirs();

        //get messages.yml and make if not exists
        File messagesFile = new File(this.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            this.saveResource("messages.yml", false);
        }

        //initialize config, messages and command classes
        this.config = new LitePlaytimeRewardsConfig();
        this.messages = new LitePlaytimeRewardsMessages(messagesFile);
        this.commands = new LitePlaytimeRewardsCommands();

        //register eventhandler class
        getServer().getPluginManager().registerEvents(new LitePlaytimeRewardsEventHandlers(), this);

        //check if essentials is installed
        this.ess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");

        //bStats
        if (this.config.isUsebStats()) {
            int pluginId = 7380;
            Metrics metrics = new Metrics(this, pluginId);
            metrics.addCustomChart(new Metrics.SimplePie("Reward Count", () -> Integer.toString(LitePlaytimeRewards.getInstance().getLPRConfig().getRewards().size())));
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        //unload variable
        plugin = null;

        //save players
        this.CRUDCache.entrySet().forEach(e -> e.getValue().saveConfig());

        //remove all runnables from plugin
        Bukkit.getScheduler().cancelTasks(this);

        super.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        return this.commands.onCommand(cs, cmnd, alias, args);
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

    public LitePlaytimeRewardsConfig getLPRConfig() {
        return this.config;
    }

    public LitePlaytimeRewardsMessages getMessages() {
        return messages;
    }

    public boolean isOldVersion() {
        return oldVersion;
    }

    public void checkForOldVersion() {
        if (this.getLPRConfig().isUpdateChecker()) {
            new UpdateChecker(71784).getVersion(version -> {
                this.oldVersion = !this.getDescription().getVersion().equalsIgnoreCase(version);
            });
        }
    }

    public void addToRunnableCache(UUID uniqueId, int taskId) {
        this.runnableCache.put(uniqueId, taskId);
    }

    public int removeFromRunnableCache(UUID uniqueId) {
        return this.runnableCache.remove(uniqueId);
    }

    public boolean doesRunnableCacheContain(UUID uniqueId) {
        return this.runnableCache.containsKey(uniqueId);
    }

    public void addToCRUDCache(UUID uniqueId, LitePlaytimeRewardsCRUD crud) {
        this.CRUDCache.put(uniqueId, crud);
    }

    public LitePlaytimeRewardsCRUD removeFromCRUDCache(UUID uniqueId) {
        return this.CRUDCache.remove(uniqueId);
    }

    public LitePlaytimeRewardsCRUD getFromCRUDCache(UUID uniqueId) {
        return this.CRUDCache.get(uniqueId);
    }

    public boolean doesCRUDCacheContain(UUID uniqueId) {
        return this.CRUDCache.containsKey(uniqueId);
    }

    public void addToGUICache(UUID id, RewardsGUI gui) {
        this.GUICache.put(id, gui);
    }

    public RewardsGUI removeFromGUICache(UUID id) {
        return this.GUICache.remove(id);
    }

    public RewardsGUI getFromGUICache(UUID id) {
        return this.GUICache.get(id);
    }

    public static LitePlaytimeRewards getInstance() {
        return plugin;
    }
}
