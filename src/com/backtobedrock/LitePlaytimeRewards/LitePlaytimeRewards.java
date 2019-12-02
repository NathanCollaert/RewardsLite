package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.eventHandlers.LitePlaytimeRewardsEventHandlers;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.ConfigReward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RedeemedReward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.UpdateChecker;
import com.backtobedrock.LitePlaytimeRewards.runnables.CheckForRewards;
import com.backtobedrock.LitePlaytimeRewards.runnables.NotifyBossBar;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class LitePlaytimeRewards extends JavaPlugin implements Listener {

    private boolean oldVersion = false;

    private LitePlaytimeRewardsConfig config;
    private LitePlaytimeRewardsCommands commands;

    private final TreeMap<UUID, TreeMap<String, RedeemedReward>> onlinePlayerListLastPlaytimeCheck = new TreeMap<>();

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(RedeemedReward.class);

        this.saveDefaultConfig();

        File dir = new File(this.getDataFolder() + "/userdata");
        dir.mkdirs();

        this.config = new LitePlaytimeRewardsConfig(this);
        this.commands = new LitePlaytimeRewardsCommands(this);

        getServer().getPluginManager().registerEvents(new LitePlaytimeRewardsEventHandlers(this), this);

        if (!this.config.getRewards().isEmpty()) {
            new CheckForRewards(this).runTaskTimer(this, 0, this.getLPRConfig().getRewardCheck() * 60 * 20);
        } else {
            this.onDisable();
        }

        this.checkForOldVersion();

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

    public boolean checkEligibleForRewards(Player plyr) {
        boolean changed = false;
        if (this.config.getDisableGettingRewardsInWorlds().contains(plyr.getWorld().getName().toLowerCase())) {
            return changed;
        }
        TreeMap<String, RedeemedReward> redeemedRewards = this.onlinePlayerListLastPlaytimeCheck.get(plyr.getUniqueId());
        LitePlaytimeRewardsCRUD crud = null;
        for (Entry<String, ConfigReward> entry : this.config.getRewards().entrySet()) {
            if (!redeemedRewards.containsKey(entry.getKey()) || entry.getValue().isLoop()) {
                crud = new LitePlaytimeRewardsCRUD(this, plyr);
                long lastPlaytimeCheck = redeemedRewards.containsKey(entry.getKey()) ? redeemedRewards.get(entry.getKey()).getLastPlaytimeCheck() : entry.getValue().isCountPlaytimeFromStart() ? 0 : crud.getPlaytimeStart();
                int playtimeCheckDifferenceInMinutes = (int) Math.floor((plyr.getStatistic(Statistic.PLAY_ONE_MINUTE) - lastPlaytimeCheck) / 20 / 60);
                if (playtimeCheckDifferenceInMinutes >= entry.getValue().getPlaytimeNeeded()) {
                    //check if enough inventory space
                    int emptySlots = 0;
                    for (ItemStack it : plyr.getInventory().getStorageContents()) {
                        if (it == null) {
                            emptySlots++;
                        }
                    }
                    if (entry.getValue().getSlotsNeeded() == 0 || emptySlots >= entry.getValue().getSlotsNeeded()) {
                        //check how many times reward needs to be given
                        int amount = playtimeCheckDifferenceInMinutes / entry.getValue().getPlaytimeNeeded();

                        this.giveRewardAndNotify(entry.getValue(), plyr, true, entry.getValue().isLoop() ? amount : 1);

                        //update or create redeemedreward
                        long newLastPlaytimeCheck = lastPlaytimeCheck + ((amount * entry.getValue().getPlaytimeNeeded()) * 60 * 20);
                        if (redeemedRewards.containsKey(entry.getKey())) {
                            RedeemedReward reward = redeemedRewards.get(entry.getKey());
                            reward.setLastPlaytimeCheck(newLastPlaytimeCheck);
                            reward.setAmountRedeemed(reward.getAmountRedeemed() + amount);
                        } else {
                            redeemedRewards.put(entry.getKey(), new RedeemedReward(newLastPlaytimeCheck, amount));
                        }
                    } else {
                        plyr.spigot().sendMessage(new ComponentBuilder("You need " + entry.getValue().getSlotsNeeded() + " open inventory slots to claim a pending reward.").color(ChatColor.GOLD).create());
                    }
                    changed = true;
                }
            }
        }

        //write data away if changes
        if (changed && crud != null) {
            crud.setRewards(redeemedRewards, true);
        }
        return changed;
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

    public void addToOnlinePlayerListLastPlaytimeCheck(UUID id, TreeMap<String, RedeemedReward> redeemedRewards) {
        this.onlinePlayerListLastPlaytimeCheck.put(id, redeemedRewards);
    }

    public void removeFromOnlinePlayerListLastPlaytimeCheck(UUID id) {
        this.onlinePlayerListLastPlaytimeCheck.remove(id);
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
}
