package com.backtobedrock.LitePlaytimeRewards.helperClasses;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RewardsGUI {

    private final OfflinePlayer player;
    private final Inventory GUI;
    private final TreeMap<String, RewardsGUIReward> GUIRewards = new TreeMap<>();

    public RewardsGUI(TreeMap<String, ?> rewards, OfflinePlayer player) {
        RewardsGUICustomHolder holder = (rewards.firstEntry().getValue() instanceof Reward
                ? new RewardsGUICustomHolder((int) (Math.ceil(((double) rewards.size()) / 9) * 9), LitePlaytimeRewards.getInstance().getMessages().getRewardsInventoryTitle())
                : new RewardsGUICustomHolder((int) (Math.ceil(((double) rewards.size()) / 9) * 9), LitePlaytimeRewards.getInstance().getMessages().getGiveRewardInventoryTitle()));
        this.GUI = holder.getInventory();
        this.player = player;
        this.initializeGUI(rewards);
    }

    private void initializeGUI(TreeMap<String, ?> rewards) {
        rewards.entrySet().stream().forEach(e -> {
            this.GUI.addItem(this.createGUIItem(e.getKey(), e.getValue()));
            this.GUIRewards.put(e.getKey(), new RewardsGUIReward());
        });
    }

    private ItemStack createGUIItem(String name, Object reward) {
        ConfigReward cReward = reward instanceof ConfigReward ? (ConfigReward) reward : ((Reward) reward).getcReward();
        ItemStack item = new ItemStack(cReward.getDisplayItem());
        ItemMeta im = item.getItemMeta();
        if (im != null) {
            im.setDisplayName(cReward.getDisplayName());
            im.setLocalizedName(name);
            List<String> description = (cReward.getDisplayDescription().stream().collect(Collectors.toList()));
            if (description.size() > 0) {
                description.add(0, "§f----");
            }
            if (reward instanceof Reward) {
                description.addAll(0, LitePlaytimeRewards.getInstance().getMessages().getRewardInfo(((Reward) reward).getAmountRedeemed(), ((Reward) reward).getAmountPending()));
                description.add("§f----");
                Long nextReward = ((Reward) reward).getTimeTillNextReward().get(0);
                description.addAll(player.isOnline() && (((Player) player).hasPermission("liteplaytimerewards.reward." + name.toLowerCase()) || !cReward.isUsePermission())
                        ? nextReward > -1L
                                ? LitePlaytimeRewards.getInstance().getMessages().getNextReward(nextReward)
                                : LitePlaytimeRewards.getInstance().getMessages().getNextRewardNever()
                        : LitePlaytimeRewards.getInstance().getMessages().getNextRewardNoPermission());
            } else {
                description.add(0, String.format("§a(amount: §2%d§a, broadcast: §2%s§a)", 1, false));
                description.add("§f----");
                description.add("§6§oRight click §e§oto toggle broadcasting");
                description.add("§6§oShift left click §e§oto increase amount");
                description.add("§6§oShift right click §e§oto decrease amount");
            }
            im.setLore(description);
            item.setItemMeta(im);
        }
        return item;
    }

    public void updateGUIItem(ItemStack item, RewardsGUIReward reward) {
        ItemStack newItem = new ItemStack(item);
        ItemMeta im = newItem.getItemMeta();
        if (im != null && im.hasLore()) {
            List<String> lore = im.getLore();
            lore.set(0, String.format("§a(amount: §2%d§a, broadcast: §2%s§a)", reward.getAmount(), reward.isBroadcast()));
            im.setLore(lore);
            newItem.setItemMeta(im);
        }
        this.GUI.setItem(this.GUI.first(item), newItem);
    }

    public Inventory getGUI() {
        return GUI;
    }

    public TreeMap<String, RewardsGUIReward> getGUIRewards() {
        return GUIRewards;
    }
}
