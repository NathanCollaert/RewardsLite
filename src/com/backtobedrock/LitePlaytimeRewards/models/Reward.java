package com.backtobedrock.LitePlaytimeRewards.models;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.runnables.NotifyBossBar;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

@SerializableAs("Reward")
public class Reward implements ConfigurationSerializable {

    private final LitePlaytimeRewards plugin;

    private List<Integer> timeTillNextReward;
    private int amountRedeemed;
    private int amountPending;
    private boolean eligible;
    private boolean claimedOldPlaytime;
    private ConfigReward cReward;

    public Reward(ConfigReward cReward, List<Integer> timeTillNextReward, int amountRedeemed, int amountPending, boolean eligible, boolean claimedOldPlaytime) {
        this(timeTillNextReward.isEmpty() ? new ArrayList(cReward.getPlaytimeNeeded()) : timeTillNextReward, Math.max(amountRedeemed, 0), Math.max(amountPending, 0), cReward.isLoop() || eligible, claimedOldPlaytime);
        this.cReward = cReward;
    }

    //deserialization only
    private Reward(List<Integer> timeTillNextReward, int amountRedeemed, int amountPending, boolean eligible, boolean claimedOldPlaytime) {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        this.timeTillNextReward = timeTillNextReward;
        this.amountRedeemed = amountRedeemed;
        this.amountPending = amountPending;
        this.eligible = eligible;
        this.claimedOldPlaytime = claimedOldPlaytime;
    }

    //new reward only
    public Reward(ConfigReward cReward) {
        this(cReward, new ArrayList<>(cReward.getPlaytimeNeeded()), 0, 0, true, false);
    }

    public static Reward deserialize(Map<String, Object> map) {
        List<Integer> timeTillNextReward = (List<Integer>) map.getOrDefault("timeTillNextReward", new ArrayList<>());
        int amountRedeemed = (int) map.getOrDefault("amountRedeemed", 0);
        int amountPending = (int) map.getOrDefault("amountPending", 0);
        boolean eligible = (boolean) map.getOrDefault("eligible", true);
        boolean claimedOldPlaytime = (boolean) map.getOrDefault("claimedOldPlaytime", false);

        //old userdata conversion (-1 to empty list)
        if (!timeTillNextReward.isEmpty() && timeTillNextReward.get(0) == -1) {
            eligible = false;
            timeTillNextReward = new ArrayList<>();
        }

        return new Reward(timeTillNextReward, amountRedeemed, amountPending, eligible, claimedOldPlaytime);
    }

    public ConfigReward getcReward() {
        return cReward;
    }

    public String getId() {
        return this.cReward.getId();
    }

    public String getName() {
        return this.cReward.getDisplayName();
    }

    public List<Integer> getTimeTillNextReward() {
        return timeTillNextReward;
    }

    public void removeFirstTimeTillNextReward() {
        this.timeTillNextReward.remove(0);
        if (this.timeTillNextReward.isEmpty()) {
            if (!this.cReward.isLoop()) {
                this.eligible = false;
            }
            this.timeTillNextReward = new ArrayList<>(this.cReward.getPlaytimeNeeded());
        }
    }

    public void resetTimeTillNextReward() {
        this.timeTillNextReward = new ArrayList<>(this.cReward.getPlaytimeNeeded());
    }

    public int getFirstTimeTillNextReward() {
        return this.timeTillNextReward.get(0);
    }

    public void setFirstTimeTillNextReward(int time) {
        this.timeTillNextReward.set(0, time);
    }

    public int getAmountRedeemed() {
        return amountRedeemed;
    }

    public void setAmountRedeemed(int amountRedeemed) {
        this.amountRedeemed = amountRedeemed;
    }

    public int getAmountPending() {
        return amountPending;
    }

    public void setAmountPending(int amountPending) {
        this.amountPending = amountPending;
    }

    public boolean isEligible() {
        return this.eligible;
    }

    public boolean isClaimedOldPlaytime() {
        return claimedOldPlaytime;
    }

    public void setClaimedOldPlaytime(boolean claimedOldPlaytime) {
        this.claimedOldPlaytime = claimedOldPlaytime;
    }

    public List<String> getRewardsGUIDescription(OfflinePlayer player) {
        List<String> description = (new ArrayList<>(this.cReward.getDisplayDescription()));

        if (description.size() > 0) {
            description.add(0, "§f----");
        }

        description.addAll(0, this.plugin.getMessages().getRewardInfo(this.getAmountRedeemed(), this.getAmountPending()));
        description.add("§f----");
        int nextReward = this.getTimeTillNextReward().get(0);
        description.addAll(player.isOnline() && (((Player) player).hasPermission("liteplaytimerewards.reward." + this.cReward.getId()) || !this.cReward.isUsePermission())
                ? this.isEligible()
                ? this.plugin.getMessages().getNextReward(nextReward)
                : this.plugin.getMessages().getNextRewardNever()
                : this.plugin.getMessages().getNextRewardNoPermission());

        return description;
    }

    public boolean hasPermission(Player player) {
        if (this.cReward.isUsePermission()) {
            return player.hasPermission("liteplaytimerewards.reward." + this.getId());
        } else {
            return true;
        }
    }

    public int giveReward(OfflinePlayer plyr, boolean broadcast, int amount) {
        String message = "";
        boolean notified = false;
        int pending = 0;
        if (plyr.isOnline()) {
            Player player = (Player) plyr;
            //give reward this amount of times
            for (int i = 0; i < amount + this.amountPending; i++) {

                //check if enough free invent space
                int emptySlots = 0;
                for (ItemStack it : player.getInventory().getStorageContents()) {
                    if (it == null) {
                        emptySlots++;
                    }
                }

                //check if in world that doesn't allow for rewards and free invent
                if (this.plugin.getLPRConfig().getDisableGettingRewardsInWorlds().contains(player.getWorld().getName().toLowerCase())) {
                    message = this.plugin.getMessages().getPendingNotificationWrongWorld(plyr.getName(), this.cReward.getDisplayName(), player.getWorld().getName());
                    pending++;
                } else if (emptySlots < this.cReward.getSlotsNeeded()) {
                    message = this.plugin.getMessages().getPendingNotificationNotEnoughInventory(plyr.getName(), this.cReward.getDisplayName(), this.cReward.getSlotsNeeded());
                    pending++;
                } else {
                    this.cReward.getCommands().forEach(j -> Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), j.replaceAll("%player%", player.getName())));

                    this.setAmountRedeemed(this.amountRedeemed + 1);

                    if (!notified) {
                        //notify user and broadcast on getting reward
                        this.notifyUsers(player, broadcast);
                        notified = true;
                    }
                }
            }

            if (!message.equals("") && amount > 0) {
                switch (this.cReward.getNotificationType()) {
                    case CHAT:
                        player.sendMessage(message);
                        break;
                    case BOSSBAR:
                        Collection<Player> players = new ArrayList<>();
                        players.add(player);
                        new NotifyBossBar(players, message, BarColor.YELLOW).runTaskTimer(this.plugin, 0, 20);
                        break;
                    case ACTIONBAR:
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(message).create());
                        break;
                }
            }
        } else {
            pending = amount + this.amountPending;
        }

        return pending;
    }

    private void notifyUsers(Player plyr, boolean broadcast) {
        String notification = this.cReward.getNotification().replaceAll("%player%", plyr.getName());
        String broadcastNotification = this.cReward.getBroadcastNotification().replaceAll("%player%", plyr.getName());

        Collection<Player> players = new ArrayList<>();

        switch (this.cReward.getNotificationType()) {
            case CHAT:
                if (!broadcastNotification.isEmpty() && broadcast) {
                    Bukkit.broadcastMessage(broadcastNotification);
                }
                if (!notification.isEmpty()) {
                    plyr.sendMessage(notification);
                }
                break;
            case BOSSBAR:
                if (!broadcastNotification.isEmpty() && broadcast) {
                    players = this.plugin.getServer().getOnlinePlayers().stream().map(e -> (Player) e).collect(Collectors.toList());
                    if (!notification.isEmpty()) {
                        players.remove(plyr);
                    }
                    new NotifyBossBar(players, broadcastNotification, BarColor.BLUE).runTaskTimer(this.plugin, 0, 20);
                }
                if (!notification.isEmpty()) {
                    players.add(plyr);
                    new NotifyBossBar(players, notification, BarColor.GREEN).runTaskTimer(this.plugin, 0, 20);
                }
                break;
            case ACTIONBAR:
                if (!broadcastNotification.isEmpty() && broadcast) {
                    players = this.plugin.getServer().getOnlinePlayers().stream().map(e -> (Player) e).collect(Collectors.toList());
                    if (!notification.isEmpty()) {
                        players.remove(plyr);
                    }
                    players.forEach(e -> e.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(broadcastNotification).create()));
                }
                if (!notification.isEmpty()) {
                    plyr.spigot().sendMessage(ChatMessageType.ACTION_BAR, new ComponentBuilder(notification).create());
                }
                break;
        }
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new TreeMap<>();

        map.put("timeTillNextReward", this.timeTillNextReward);
        map.put("amountRedeemed", this.amountRedeemed);
        map.put("amountPending", this.amountPending);
        map.put("eligible", this.eligible);
        map.put("claimedOldPlaytime", this.claimedOldPlaytime);

        return map;
    }
}
