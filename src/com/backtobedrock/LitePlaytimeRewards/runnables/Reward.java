package com.backtobedrock.LitePlaytimeRewards.runnables;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.RedeemedReward;
import com.earth2me.essentials.User;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Reward extends BukkitRunnable {
    
    private final LitePlaytimeRewards plugin;
    private final Player plyr;
    private final List<RedeemedReward> rewards;
    private User user = null;
    
    public Reward(LitePlaytimeRewards plugin, Player plyr, TreeMap<String, RedeemedReward> rewards) {
        this.plugin = plugin;
        this.plyr = plyr;
        this.rewards = rewards.values().stream().filter(e -> e.getTimeTillNextReward()!= -1).collect(Collectors.toList());
        if (plugin.ess != null) {
            this.user = plugin.ess.getUser(plyr);
        }
    }
    
    @Override
    public void run() {
        if (user != null) {
            this.checkRewards(false);
            //add playtime
        } else {
            this.checkRewards(user.isAfk());
            if (user.isAfk()) {
                //add afktime
            } else {
                //add playtime
            }
        }
    }
    
    private void checkRewards(boolean isAfk) {
        
    }
    
}
