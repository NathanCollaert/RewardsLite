package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.eventHandlers.LitePlaytimeRewardsEventHandlers;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.Reward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.UpdateChecker;
import com.backtobedrock.LitePlaytimeRewards.runnables.CheckForRewards;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class LitePlaytimeRewards extends JavaPlugin implements Listener {

    private final boolean oldVersion = false;
    private TreeMap<String, Reward> rewards;

    private LitePlaytimeRewardsConfig config;
    private LitePlaytimeRewardsCommands commands;

    private final TreeMap<UUID, TreeMap<String, Long>> onlinePlayerListLastPlaytimeCheck = new TreeMap<>();

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        File dir = new File(this.getDataFolder() + "/userdata");
        dir.mkdirs();

        this.config = new LitePlaytimeRewardsConfig(this);
        this.rewards = this.config.getRewards();
        this.commands = new LitePlaytimeRewardsCommands(this);

//        if (this.getLDBConfig().isUpdateChecker()) {
//            new UpdateChecker(this,).getVersion(version -> {
//                this.oldVersion = !this.getDescription().getVersion().equalsIgnoreCase(version);
//            });
//        }
        getServer().getPluginManager().registerEvents(new LitePlaytimeRewardsEventHandlers(this), this);

        if (!this.getLPRConfig().getRewards().isEmpty()) {
            new CheckForRewards(this).runTaskTimer(this, 0, this.getLPRConfig().getRewardCheck() * 60 * 20);
        }

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
        TreeMap<String, Long> redeemedRewards = this.onlinePlayerListLastPlaytimeCheck.get(plyr.getUniqueId());
        for (Entry<String, Reward> entry : this.rewards.entrySet()) {
            if (!redeemedRewards.containsKey(entry.getKey()) || entry.getValue().isLoop()) {
                long lastPlaytimeCheck = this.onlinePlayerListLastPlaytimeCheck.get(plyr.getUniqueId()).get(entry.getKey()) == null ? 0 : this.onlinePlayerListLastPlaytimeCheck.get(plyr.getUniqueId()).get(entry.getKey());
                int playtimeCheckDifferenceInMinutes = (int) Math.floor((plyr.getStatistic(Statistic.PLAY_ONE_MINUTE) - lastPlaytimeCheck) / 20 / 60);
                System.out.println(lastPlaytimeCheck);
                System.out.println(playtimeCheckDifferenceInMinutes);
                if (playtimeCheckDifferenceInMinutes >= entry.getValue().getPlaytimeNeeded()) {
                    //check how many times reward needs to be given
                    int amount = playtimeCheckDifferenceInMinutes / entry.getValue().getPlaytimeNeeded();
                    System.out.println(amount);

                    //give reward this amount of times
                    for (int i = 0; i < amount; i++) {
                        entry.getValue().getCommands().forEach(j -> {
                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), j.replaceAll("%player%", plyr.getName()));
                        });
                    }

                    //notify user and broadcast on getting reward
                    String notification = entry.getValue().getNotification().replaceAll("%player%", plyr.getName());
                    String broadcastNotification = entry.getValue().getBroadcastNotification().replaceAll("%player%", plyr.getName());
                    if (!notification.isEmpty()) {
                        plyr.spigot().sendMessage(new ComponentBuilder(notification).create());
                    }
                    if (!broadcastNotification.isEmpty()) {
                        Bukkit.broadcastMessage(broadcastNotification);
                    }

                    //update last playtime check for reward for player and notify changes
                    long newLastPlaytimeCheck = lastPlaytimeCheck + ((amount * entry.getValue().getPlaytimeNeeded()) * 60 * 20);
                    redeemedRewards.put(entry.getKey(), newLastPlaytimeCheck);
                    changed = true;
                }
            }
        };

        //write data away if changes
        if (changed) {
            LitePlaytimeRewardsCRUD crud = new LitePlaytimeRewardsCRUD(this, plyr);
            crud.setRedeemedRewards(redeemedRewards, true);
        }
        return changed;
    }

    public LitePlaytimeRewardsConfig getLPRConfig() {
        return this.config;
    }

    public void addToOnlinePlayerListLastPlaytimeCheck(UUID id, TreeMap<String, Long> rewardsLastPlaytimeCheck) {
        this.onlinePlayerListLastPlaytimeCheck.put(id, rewardsLastPlaytimeCheck);
    }

    public void removeFromOnlinePlayerListLastPlaytimeCheck(UUID id) {
        this.onlinePlayerListLastPlaytimeCheck.remove(id);
    }

    public boolean isOldVersion() {
        return oldVersion;
    }
}
