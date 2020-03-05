package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.eventHandlers.LitePlaytimeRewardsEventHandlers;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.ConfigReward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.Reward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.UpdateChecker;
import com.backtobedrock.LitePlaytimeRewards.runnables.NotifyBossBar;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import net.ess3.api.IEssentials;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
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

    private LitePlaytimeRewardsConfig config;
    private LitePlaytimeRewardsCommands commands;

    private TreeMap<UUID, Integer> runningRewards;

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(Reward.class);

        this.saveDefaultConfig();

        File dir = new File(this.getDataFolder() + "/userdata");
        dir.mkdirs();

        this.config = new LitePlaytimeRewardsConfig(this);
        this.commands = new LitePlaytimeRewardsCommands(this);

        getServer().getPluginManager().registerEvents(new LitePlaytimeRewardsEventHandlers(this), this);

        this.checkForOldVersion();
        ess = (IEssentials) Bukkit.getPluginManager().getPlugin("EssentialsX");

        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        return this.commands.onCommand(cs, cmnd, alias, args);
    }

    public boolean checkEligibleForRewards(Player plyr, TreeMap<String, Reward> rewards) {
        boolean notified = false;
        for (Entry<String, Reward> entry : rewards.entrySet()) {
            ConfigReward cReward = this.config.getRewards().get(entry.getKey());

            if (cReward == null) {
                break;
            }

            if (this.config.getDisableGettingRewardsInWorlds().contains(plyr.getLocation().getWorld().getName())) {
                if (!notified) {
                    plyr.spigot().sendMessage(new ComponentBuilder("You have a reward pending but you can't claim it in this world!").color(ChatColor.GOLD).create());
                }
                notified = true;
                break;
            }

            int emptySlots = 0;
            for (ItemStack it : plyr.getInventory().getStorageContents()) {
                if (it == null) {
                    emptySlots++;
                }
            }
            if (emptySlots >= cReward.getSlotsNeeded()) {
                this.giveRewardAndNotify(cReward, plyr, true, 1);
                entry.getValue().setAmountRedeemed(entry.getValue().getAmountRedeemed() + 1);
                entry.getValue().getTimeTillNextReward().remove(0);
                if (cReward.isLoop()) {

                }
            } else {
                if (!notified) {
                    plyr.spigot().sendMessage(new ComponentBuilder("You need " + cReward.getSlotsNeeded() + " open inventory slots to claim a pending reward.").color(ChatColor.GOLD).create());
                    notified = true;
                    break;
                }
            }
        }
        return notified;
    }

    public void giveRewardAndNotify(ConfigReward reward, Player plyr, boolean broadcast, int amount) {
        //give reward this amount of times
        for (int i = 0; i < amount; i++) {
            reward.getCommands().forEach(j -> {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), j.replaceAll("%player%", plyr.getName()));
            });
        }

        //notify user and broadcast on getting reward
        this.notifyUsers(reward, plyr);
    }

    private void notifyUsers(ConfigReward reward, Player plyr) {
        String notification = reward.getNotification().replaceAll("%player%", plyr.getName());
        String broadcastNotification = reward.getBroadcastNotification().replaceAll("%player%", plyr.getName());

        Collection<Player> players = new ArrayList<>();

        switch (reward.getNotificationType().toLowerCase()) {
            case "chat":
                if (!broadcastNotification.isEmpty()) {
                    Bukkit.broadcastMessage(broadcastNotification);
                }
                if (!notification.isEmpty()) {
                    plyr.spigot().sendMessage(new ComponentBuilder(notification).create());
                }
                break;
            case "bossbar":
                if (!broadcastNotification.isEmpty()) {
                    players = this.getServer().getOnlinePlayers().stream().map(e -> (Player) e).collect(Collectors.toList());
                    if (!notification.isEmpty()) {
                        players.remove(plyr);
                    }
                    new NotifyBossBar(this, players, broadcastNotification).runTaskTimer(this, 0, 20);
                }
                if (!notification.isEmpty()) {
                    players.add(plyr);
                    new NotifyBossBar(this, players, notification).runTaskTimer(this, 0, 20);
                }
                break;
            case "actionbar":
                if (!broadcastNotification.isEmpty()) {
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
            new UpdateChecker(this, 71784).getVersion(version -> {
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
}
