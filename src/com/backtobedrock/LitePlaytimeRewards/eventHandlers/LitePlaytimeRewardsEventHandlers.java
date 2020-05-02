package com.backtobedrock.LitePlaytimeRewards.eventHandlers;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsCRUD;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsCommands;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsConfig;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.Reward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RewardsGUI;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RewardsGUICustomHolder;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RewardsGUIReward;
import com.backtobedrock.LitePlaytimeRewards.runnables.Countdown;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import static java.util.stream.Collectors.toMap;
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
import org.bukkit.scheduler.BukkitTask;

public class LitePlaytimeRewardsEventHandlers implements Listener {

    private final LitePlaytimeRewards plugin;
    private final LitePlaytimeRewardsConfig config;

    private final TreeMap<UUID, String> isGivingReward = new TreeMap<>();

    public LitePlaytimeRewardsEventHandlers() {
        this.plugin = LitePlaytimeRewards.getInstance();
        this.config = this.plugin.getLPRConfig();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        LitePlaytimeRewardsCRUD crud = new LitePlaytimeRewardsCRUD(e.getPlayer());

        //check if config has untracked rewards
        TreeMap<String, Reward> rewards = new TreeMap<>();
        this.config.getRewards().entrySet().stream().forEach(f -> {
            if (crud.getRewards().containsKey(f.getKey()) && (!f.getValue().isUsePermission() || e.getPlayer().hasPermission("liteplaytimerewards.reward." + f.getKey()))) {
                rewards.put(f.getKey(), crud.getRewards().get(f.getKey()));
            } else if (!f.getValue().isUsePermission() || e.getPlayer().hasPermission("liteplaytimerewards.reward." + f.getKey())) {
                rewards.put(f.getKey(), new Reward(f.getValue(), f.getKey(), f.getValue().getPlaytimeNeeded(), 0, 0));
            }
        });
        crud.setRewards(rewards, true);

        //filter what rewards should be ran
        TreeMap<String, Reward> loopRewards = rewards.entrySet().stream()
                .filter(r -> !r.getValue().getTimeTillNextReward().get(0).equals(-1L) || r.getValue().getAmountPending() > 0)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, TreeMap::new));

        //run task, add task to map and crud to online players
        this.plugin.addToOnlineCRUDs(e.getPlayer().getUniqueId(), crud);
        BukkitTask rewardRunnable = new Countdown(20L, e.getPlayer(), loopRewards, crud).runTaskTimer(this.plugin, 20, 20);
        this.plugin.addToRunningRewards(e.getPlayer().getUniqueId(), rewardRunnable.getTaskId());

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
        //remove from online crud, cancel task, remove task from map
        this.plugin.removeFromOnlineCRUDs(e.getPlayer().getUniqueId()).saveConfig();
        Bukkit.getScheduler().cancelTask(this.plugin.getFromRunningRewards(e.getPlayer().getUniqueId()));
        this.plugin.removeFromRunningRewards(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player plyr = (Player) e.getWhoClicked();
        Inventory openInv = e.getView().getTopInventory();

        if (openInv.getHolder() instanceof RewardsGUICustomHolder && ((RewardsGUICustomHolder) openInv.getHolder()).getTitle().equals("Available Rewards")) {
            e.setCancelled(true);

            RewardsGUI gui = this.plugin.getFromGUIs(plyr.getUniqueId());

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
        } else if (openInv.getHolder() instanceof RewardsGUICustomHolder && ((RewardsGUICustomHolder) openInv.getHolder()).getTitle().equals("Your Rewards")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player plyr = (Player) e.getPlayer();
        Inventory openInv = e.getView().getTopInventory();

        if (openInv.getHolder() instanceof RewardsGUICustomHolder && !this.isGivingReward.containsKey(plyr.getUniqueId())) {
            this.plugin.removeFromGUIs(plyr.getUniqueId());
        }
    }

    @EventHandler
    public void OnPlayerChatEvent(AsyncPlayerChatEvent e) {
        Player plyr = e.getPlayer();

        //Check if player chatting is using reward GUI
        if (this.isGivingReward.containsKey(plyr.getUniqueId())) {

            e.setCancelled(true);

            String rewardName = this.isGivingReward.get(plyr.getUniqueId());
            RewardsGUIReward reward = this.plugin.getFromGUIs(plyr.getUniqueId()).getGUIRewards().get(rewardName);

            Bukkit.getScheduler().runTask(this.plugin, () -> {
                if (e.getMessage().equalsIgnoreCase("!cancel")) {
                    this.isGivingReward.remove(plyr.getUniqueId());
                    plyr.openInventory(this.plugin.getFromGUIs(plyr.getUniqueId()).getGUI());
                } else if (LitePlaytimeRewardsCommands.giveRewardCommand(plyr, rewardName, e.getMessage(), reward.isBroadcast(), reward.getAmount())) {
                    plyr.spigot().sendMessage(new ComponentBuilder("The " + rewardName + " reward has been given to " + e.getMessage() + ".").color(ChatColor.GREEN).create());
                    this.isGivingReward.remove(plyr.getUniqueId());
                    this.plugin.removeFromGUIs(plyr.getUniqueId());
                } else {
                    plyr.openInventory(this.plugin.getFromGUIs(plyr.getUniqueId()).getGUI());
                }
            });
        }
    }
}
