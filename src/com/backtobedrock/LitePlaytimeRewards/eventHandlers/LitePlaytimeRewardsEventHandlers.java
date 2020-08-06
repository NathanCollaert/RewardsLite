package com.backtobedrock.LitePlaytimeRewards.eventHandlers;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.configs.PlayerData;
import com.backtobedrock.LitePlaytimeRewards.configs.Config;
import com.backtobedrock.LitePlaytimeRewards.commands.GiverewardCommand;
import com.backtobedrock.LitePlaytimeRewards.enums.GUIType;
import com.backtobedrock.LitePlaytimeRewards.guis.CustomHolder;
import com.backtobedrock.LitePlaytimeRewards.guis.Icon;
import com.backtobedrock.LitePlaytimeRewards.models.GUIReward;
import com.backtobedrock.LitePlaytimeRewards.runnables.Countdown;
import java.util.TreeMap;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class LitePlaytimeRewardsEventHandlers implements Listener {

    private final LitePlaytimeRewards plugin;
    private final Config config;

    private final TreeMap<UUID, Integer> liveCounter = new TreeMap<>();

    public LitePlaytimeRewardsEventHandlers() {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        this.config = this.plugin.getLPRConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        //check if cached crud
        if (!this.plugin.getPlayerCache().containsKey(e.getPlayer().getUniqueId())) {
            //get crud and add to cache
            PlayerData data = new PlayerData(e.getPlayer());
            this.plugin.getPlayerCache().put(e.getPlayer().getUniqueId(), data);
        } else {
            Bukkit.getScheduler().cancelTask(this.plugin.getRunnableCache().remove(e.getPlayer().getUniqueId()));
        }

        //run task, add task to cache
        BukkitTask rewardRunnable = new Countdown(20, e.getPlayer()).runTaskTimer(this.plugin, 20, 20);
        this.plugin.getRunnableCache().put(e.getPlayer().getUniqueId(), rewardRunnable.getTaskId());

        //check for updates
        if (this.config.isUpdateChecker() && e.getPlayer().isOp()) {
            this.plugin.checkForOldVersion();
            if (this.plugin.isOldVersion()) {
                e.getPlayer().spigot().sendMessage(new ComponentBuilder("There is a new version of LitePlaytimeRewards available ").color(ChatColor.YELLOW).append("here").color(ChatColor.AQUA).event(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/liteplaytimerewards-give-rewards-based-on-playtime-with-ease.71784/history")).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Go to spigotmc.org").create())).append(".").color(ChatColor.YELLOW).create());
            }
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        //cancel task and remove from cache
        Bukkit.getScheduler().cancelTask(this.plugin.getRunnableCache().remove(e.getPlayer().getUniqueId()));

        //save player data
        this.plugin.getPlayerCache().get(e.getPlayer().getUniqueId()).saveConfig();

        //cancel give reward if giving
        this.plugin.getIsGiving().remove(e.getPlayer().getUniqueId());

        //shedule task to clear cache
        BukkitTask cacheRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!e.getPlayer().isOnline()) {
                    //remove from crud cache
                    JavaPlugin.getPlugin(LitePlaytimeRewards.class).getPlayerCache().remove(e.getPlayer().getUniqueId());
                    JavaPlugin.getPlugin(LitePlaytimeRewards.class).getRunnableCache().remove(e.getPlayer().getUniqueId());
                }
            }
        }.runTaskLater(this.plugin, this.config.getTimeKeepDataInCache());

        //add remove from cache id to cache
        this.plugin.getRunnableCache().put(e.getPlayer().getUniqueId(), cacheRunnable.getTaskId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        //Check if the inventory is custom
        if (event.getView().getTopInventory().getHolder() instanceof CustomHolder) {
            //Cancel the event
            event.setCancelled(true);

            //Check if who clicked is a Player
            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();

                //Check if the item the player clicked on is valid
                ItemStack itemStack = event.getCurrentItem();
                if (itemStack == null || itemStack.getType() == Material.AIR) {
                    return;
                }

                //Get our CustomHolder
                CustomHolder customHolder = (CustomHolder) event.getView().getTopInventory().getHolder();

                //Check if the clicked slot is any icon
                Icon icon = customHolder.getIcon(event.getRawSlot());
                if (icon == null) {
                    return;
                }

                //Execute all the actions
                icon.getClickActions().forEach(clickAction -> {
                    clickAction.execute(player, event.getClick());
                });
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player plyr = (Player) event.getPlayer();
        Inventory openInv = event.getView().getTopInventory();

        if (openInv.getHolder() instanceof CustomHolder && ((CustomHolder) openInv.getHolder()).getType() == GUIType.REWARDS) {
            BukkitTask liveCounterTask = new BukkitRunnable() {
                @Override
                public void run() {
                    CustomHolder holder = (CustomHolder) openInv.getHolder();
                    holder.updateRewards(plyr);
                    plyr.updateInventory();
                }
            }.runTaskTimer(this.plugin, 20, 20);
            this.liveCounter.put(plyr.getUniqueId(), liveCounterTask.getTaskId());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player plyr = (Player) e.getPlayer();
        Inventory openInv = e.getView().getTopInventory();

        if (openInv.getHolder() instanceof CustomHolder) {
            CustomHolder holder = (CustomHolder) openInv.getHolder();
            switch (holder.getType()) {
                case REWARDS:
                    if (this.liveCounter.containsKey(plyr.getUniqueId())) {
                        Bukkit.getScheduler().cancelTask(this.liveCounter.remove(plyr.getUniqueId()));
                    }
                    break;
                case GIVE_REWARD:
                    if (!this.plugin.getIsGiving().containsKey(plyr.getUniqueId())) {
                        this.plugin.getGUICache().remove(plyr.getUniqueId());
                    }
                    break;
            }
        }
    }

    @EventHandler
    public void OnPlayerChatEvent(AsyncPlayerChatEvent e) {
        Player plyr = e.getPlayer();

        //Check if player chatting is using reward GUI
        if (this.plugin.getIsGiving().containsKey(plyr.getUniqueId())) {

            e.setCancelled(true);

            GUIReward reward = this.plugin.getIsGiving().get(plyr.getUniqueId());
            String rewardName = reward.getId();

            Bukkit.getScheduler().runTask(this.plugin, () -> {
                if (e.getMessage().equalsIgnoreCase("!cancel")) {
                    this.plugin.getIsGiving().remove(plyr.getUniqueId());
                    plyr.openInventory(this.plugin.getGUICache().get(plyr.getUniqueId()).getInventory());
                } else if (GiverewardCommand.giveRewardCommand(plyr, rewardName, e.getMessage(), reward.isBroadcast(), reward.getAmount())) {
                    plyr.sendMessage(this.plugin.getMessages().getRewardGiven(e.getMessage(), rewardName));
                    this.plugin.getIsGiving().remove(plyr.getUniqueId());
                    this.plugin.getGUICache().remove(plyr.getUniqueId());
                } else {
                    plyr.openInventory(this.plugin.getGUICache().get(plyr.getUniqueId()).getInventory());
                }
            });
        }
    }
}
