package com.backtobedrock.LitePlaytimeRewards.eventHandlers;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsCRUD;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsConfig;
import com.backtobedrock.LitePlaytimeRewards.commands.GiverewardCommand;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RewardsGUI;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RewardsGUICustomHolder;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RewardsGUIReward;
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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class LitePlaytimeRewardsEventHandlers implements Listener {

    private final LitePlaytimeRewards plugin;
    private final LitePlaytimeRewardsConfig config;

    private final TreeMap<UUID, String> isGivingReward = new TreeMap<>();

    public LitePlaytimeRewardsEventHandlers() {
        this.plugin = JavaPlugin.getPlugin(LitePlaytimeRewards.class);
        this.config = this.plugin.getLPRConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        //check if cached crud
        if (!this.plugin.doesCRUDCacheContain(e.getPlayer().getUniqueId())) {
            //get crud and add to cache
            LitePlaytimeRewardsCRUD crud = new LitePlaytimeRewardsCRUD(e.getPlayer());
            this.plugin.addToCRUDCache(e.getPlayer().getUniqueId(), crud);
        } else {
            Bukkit.getScheduler().cancelTask(this.plugin.removeFromRunnableCache(e.getPlayer().getUniqueId()));
        }

        //run task, add task to cache
        BukkitTask rewardRunnable = new Countdown(20, e.getPlayer()).runTaskTimer(this.plugin, 20, 20);
        this.plugin.addToRunnableCache(e.getPlayer().getUniqueId(), rewardRunnable.getTaskId());

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
        Bukkit.getScheduler().cancelTask(this.plugin.removeFromRunnableCache(e.getPlayer().getUniqueId()));

        //save player data
        this.plugin.getFromCRUDCache(e.getPlayer().getUniqueId()).saveConfig();

        //cancel give reward if giving
        this.isGivingReward.remove(e.getPlayer().getUniqueId());

        //shedule task to clear cache
        BukkitTask cacheRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!e.getPlayer().isOnline()) {
                    //remove from crud cache
                    JavaPlugin.getPlugin(LitePlaytimeRewards.class).removeFromCRUDCache(e.getPlayer().getUniqueId());
                    JavaPlugin.getPlugin(LitePlaytimeRewards.class).removeFromRunnableCache(e.getPlayer().getUniqueId());
                }
            }
        }.runTaskLater(this.plugin, this.config.getTimeKeepDataInCache());

        //add remove from cache id to cache
        this.plugin.addToRunnableCache(e.getPlayer().getUniqueId(), cacheRunnable.getTaskId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player plyr = (Player) e.getWhoClicked();
        Inventory openInv = e.getView().getTopInventory();

        if (openInv.getHolder() instanceof RewardsGUICustomHolder && ((RewardsGUICustomHolder) openInv.getHolder()).getTitle().equals(this.plugin.getMessages().getGiveRewardInventoryTitle())) {
            e.setCancelled(true);

            RewardsGUI gui = this.plugin.getFromGUICache(plyr.getUniqueId());

            if (e.getCurrentItem() != null) {
                //get clicked item and ItemMeta
                ItemStack ci = e.getCurrentItem();
                ItemMeta im = ci.getItemMeta();

                // verify current item is not air and has localized name and if localized name is contained in config
                if (ci.getType() != Material.AIR && im != null && im.hasLocalizedName() && config.getRewards().containsKey(im.getLocalizedName())) {
                    RewardsGUIReward reward = gui.getGUIRewards().get(im.getLocalizedName());
                    //if right click and shift, decrease amount
                    if (e.isRightClick() && e.isShiftClick()) {
                        reward.decreaseAmount();
                        gui.updateGUIItem(ci, reward);
                        Bukkit.getScheduler().runTask(this.plugin, () -> plyr.updateInventory());
                        //if left click and shift, increase amount
                    } else if (e.isLeftClick() && e.isShiftClick()) {
                        reward.increaseAmount();
                        gui.updateGUIItem(ci, reward);
                        Bukkit.getScheduler().runTask(this.plugin, () -> plyr.updateInventory());
                        //if right click, switch broadcast
                    } else if (e.isRightClick()) {
                        reward.toggleBroadcast();
                        gui.updateGUIItem(ci, reward);
                        Bukkit.getScheduler().runTask(this.plugin, () -> plyr.updateInventory());
                        //if left click, give reward
                    } else if (e.isLeftClick()) {
                        this.isGivingReward.put(plyr.getUniqueId(), im.getLocalizedName());
                        Bukkit.getScheduler().runTask(this.plugin, () -> plyr.closeInventory());
                        plyr.spigot().sendMessage(new ComponentBuilder("Who do you want to give the reward to?").color(ChatColor.AQUA).create());
                        plyr.spigot().sendMessage(new ComponentBuilder("Please Send the playername in chat (").color(ChatColor.AQUA).append("!cancel").color(ChatColor.DARK_AQUA).append(" to cancel).").color(ChatColor.AQUA).create());
                    }
                }
            }
        } else if (openInv.getHolder() instanceof RewardsGUICustomHolder && ((RewardsGUICustomHolder) openInv.getHolder()).getTitle().equals(this.plugin.getMessages().getRewardsInventoryTitle())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player plyr = (Player) e.getPlayer();
        Inventory openInv = e.getView().getTopInventory();

        if (openInv.getHolder() instanceof RewardsGUICustomHolder && !this.isGivingReward.containsKey(plyr.getUniqueId())) {
            this.plugin.removeFromGUICache(plyr.getUniqueId());
        }
    }

    @EventHandler
    public void OnPlayerChatEvent(AsyncPlayerChatEvent e) {
        Player plyr = e.getPlayer();

        //Check if player chatting is using reward GUI
        if (this.isGivingReward.containsKey(plyr.getUniqueId())) {

            e.setCancelled(true);

            String rewardName = this.isGivingReward.get(plyr.getUniqueId());
            RewardsGUIReward reward = this.plugin.getFromGUICache(plyr.getUniqueId()).getGUIRewards().get(rewardName);

            Bukkit.getScheduler().runTask(this.plugin, () -> {
                if (e.getMessage().equalsIgnoreCase("!cancel")) {
                    this.isGivingReward.remove(plyr.getUniqueId());
                    plyr.openInventory(this.plugin.getFromGUICache(plyr.getUniqueId()).getGUI());
                } else if (GiverewardCommand.giveRewardCommand(plyr, rewardName, e.getMessage(), reward.isBroadcast(), reward.getAmount())) {
                    plyr.sendMessage(this.plugin.getMessages().getRewardGiven(e.getMessage(), rewardName));
                    this.isGivingReward.remove(plyr.getUniqueId());
                    this.plugin.removeFromGUICache(plyr.getUniqueId());
                } else {
                    plyr.openInventory(this.plugin.getFromGUICache(plyr.getUniqueId()).getGUI());
                }
            });
        }
    }
}
