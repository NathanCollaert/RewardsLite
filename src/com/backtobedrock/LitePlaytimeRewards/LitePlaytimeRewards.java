package com.backtobedrock.LitePlaytimeRewards;

import com.backtobedrock.LitePlaytimeRewards.eventHandlers.LitePlaytimeRewardsEventHandlers;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.ConfigReward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RedeemedReward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.UpdateChecker;
import com.backtobedrock.LitePlaytimeRewards.runnables.CheckForRewards;
import java.io.File;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

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

        if (this.getLPRConfig().isUpdateChecker()) {
            new UpdateChecker(this, 71784).getVersion(version -> {
                this.oldVersion = !this.getDescription().getVersion().equalsIgnoreCase(version);
            });
        }

        getServer().getPluginManager().registerEvents(new LitePlaytimeRewardsEventHandlers(this), this);

        if (!this.config.getRewards().isEmpty()) {
            new CheckForRewards(this).runTaskTimer(this, 0, this.getLPRConfig().getRewardCheck() * 60 * 20);
        } else {
            this.onDisable();
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
        TreeMap<String, RedeemedReward> redeemedRewards = this.onlinePlayerListLastPlaytimeCheck.get(plyr.getUniqueId());
        LitePlaytimeRewardsCRUD crud = null;
        for (Entry<String, ConfigReward> entry : this.config.getRewards().entrySet()) {
            if (!redeemedRewards.containsKey(entry.getKey()) || entry.getValue().isLoop()) {
                crud = new LitePlaytimeRewardsCRUD(this, plyr);
                long lastPlaytimeCheck = redeemedRewards.containsKey(entry.getKey()) ? redeemedRewards.get(entry.getKey()).getLastPlaytimeCheck() : entry.getValue().isCountPlaytimeFromStart() ? 0 : crud.getPlaytimeStart();
                int playtimeCheckDifferenceInMinutes = (int) Math.floor((plyr.getStatistic(Statistic.PLAY_ONE_MINUTE) - lastPlaytimeCheck) / 20 / 60);
                if (playtimeCheckDifferenceInMinutes >= entry.getValue().getPlaytimeNeeded()) {
                    //check how many times reward needs to be given
                    int amount = playtimeCheckDifferenceInMinutes / entry.getValue().getPlaytimeNeeded();

                    this.giveRewardAndNotify(entry.getValue(), plyr, true, amount);

                    //update or create redeemedreward
                    long newLastPlaytimeCheck = lastPlaytimeCheck + ((amount * entry.getValue().getPlaytimeNeeded()) * 60 * 20);
                    if (redeemedRewards.containsKey(entry.getKey())) {
                        RedeemedReward reward = redeemedRewards.get(entry.getKey());
                        reward.setLastPlaytimeCheck(newLastPlaytimeCheck);
                        reward.setAmountRedeemed(reward.getAmountRedeemed() + amount);
                    } else {
                        redeemedRewards.put(entry.getKey(), new RedeemedReward(newLastPlaytimeCheck, amount));
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
        String notification = reward.getNotification().replaceAll("%player%", plyr.getName());
        String broadcastNotification = reward.getBroadcastNotification().replaceAll("%player%", plyr.getName());
        if (!notification.isEmpty()) {
            plyr.spigot().sendMessage(new ComponentBuilder(notification).create());
        }
        if (!broadcastNotification.isEmpty() && broadcast) {
            Bukkit.broadcastMessage(broadcastNotification);
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
}
