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
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import net.ess3.api.IEssentials;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
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
    private LitePlaytimeRewardsCommands commands;

    private TreeMap<UUID, Integer> runningRewards;
    private TreeMap<UUID, LitePlaytimeRewardsCRUD> onlineCRUDs;
    private TreeMap<UUID, RewardsGUI> GUIs;

    @Override
    public void onEnable() {
        plugin = this;

        this.runningRewards = new TreeMap<>();
        this.onlineCRUDs = new TreeMap<>();
        this.GUIs = new TreeMap<>();

        ConfigurationSerialization.registerClass(Reward.class);

        this.saveDefaultConfig();

        File dir = new File(this.getDataFolder() + "/userdata");
        dir.mkdirs();

        this.config = new LitePlaytimeRewardsConfig();
        this.commands = new LitePlaytimeRewardsCommands();

        getServer().getPluginManager().registerEvents(new LitePlaytimeRewardsEventHandlers(), this);

        this.ess = (IEssentials) Bukkit.getPluginManager().getPlugin("Essentials");

        if (this.config.isUsebStats()) {
            int pluginId = 7380;
            Metrics metrics = new Metrics(this, pluginId);
            metrics.addCustomChart(new Metrics.SimplePie("reward_count", () -> Integer.toString(LitePlaytimeRewards.getInstance().getLPRConfig().getRewards().size())));
        }

        super.onEnable();
    }

    @Override
    public void onDisable() {
        plugin = null;
        this.onlineCRUDs.entrySet().forEach(e -> e.getValue().saveConfig());
        super.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        return this.commands.onCommand(cs, cmnd, alias, args);
    }

    public int giveReward(Reward reward, Player plyr, boolean broadcast, int amount) {
        String message = "";
        boolean notified = false;
        int pending = 0;

        //give reward this amount of times
        for (int i = 0; i < amount + reward.getAmountPending(); i++) {
            //check if enough free invent space
            int emptySlots = 0;
            for (ItemStack it : plyr.getInventory().getStorageContents()) {
                if (it == null) {
                    emptySlots++;
                }
            }

            //check if in world that doesn't allow for rewards and free invent
            if (this.config.getDisableGettingRewardsInWorlds().contains(plyr.getWorld().getName().toLowerCase())) {
                message = "§eYou have a playtime reward pending but can't claim it in this world.";
                pending++;
            } else if (emptySlots < reward.getSlotsNeeded()) {
                message = "§eYou need §6" + reward.getSlotsNeeded() + " §efree inventory spaces to claim a pending playtime reward.";
                pending++;
            } else {
                reward.getCommands().forEach(j -> {
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), j.replaceAll("%player%", plyr.getName()));
                });

                reward.setAmountRedeemed(reward.getAmountRedeemed() + 1);

                if (!notified) {
                    //notify user and broadcast on getting reward
                    this.notifyUsers(reward, plyr, broadcast);
                    notified = true;
                }
            }
        }

        if (!message.equals("") && amount > 0) {
            switch (reward.getNotificationType().toLowerCase()) {
                case "chat":
                    plyr.spigot().sendMessage(new ComponentBuilder(message).create());
                    break;
                case "bossbar":
                    Collection<Player> players = new ArrayList<>();
                    players.add(plyr);
                    new NotifyBossBar(players, message, BarColor.YELLOW).runTaskTimer(this, 0, 20);
                    break;
                case "actionbar":
                    plyr.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(message).create());
                    break;
            }
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
                    plyr.spigot().sendMessage(new ComponentBuilder(notification).create());
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

    public void addToRunningRewards(UUID uniqueId, int taskId) {
        this.runningRewards.put(uniqueId, taskId);
    }

    public int getFromRunningRewards(UUID uniqueId) {
        return this.runningRewards.get(uniqueId);
    }

    public void removeFromRunningRewards(UUID uniqueId) {
        this.runningRewards.remove(uniqueId);
    }

    public boolean doesRunningRewardsContain(UUID uniqueId) {
        return this.runningRewards.containsKey(uniqueId);
    }

    public void addToOnlineCRUDs(UUID uniqueId, LitePlaytimeRewardsCRUD crud) {
        this.onlineCRUDs.put(uniqueId, crud);
    }

    public LitePlaytimeRewardsCRUD removeFromOnlineCRUDs(UUID uniqueId) {
        return this.onlineCRUDs.remove(uniqueId);
    }

    public LitePlaytimeRewardsCRUD getFromOnlineCRUDs(UUID uniqueId) {
        return this.onlineCRUDs.get(uniqueId);
    }

    public void addToGUIs(UUID id, RewardsGUI gui) {
        this.GUIs.put(id, gui);
    }

    public RewardsGUI removeFromGUIs(UUID id) {
        return this.GUIs.remove(id);
    }

    public RewardsGUI getFromGUIs(UUID id) {
        return this.GUIs.get(id);
    }

    public static LitePlaytimeRewards getInstance() {
        return plugin;
    }
}
