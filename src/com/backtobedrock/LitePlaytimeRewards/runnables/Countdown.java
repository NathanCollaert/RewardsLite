package com.backtobedrock.LitePlaytimeRewards.runnables;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.ConfigReward;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.Reward;
import com.earth2me.essentials.User;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Countdown extends BukkitRunnable {

    private final LitePlaytimeRewards plugin;
    private final Player plyr;
    private final TreeMap<String, Reward> rewards;
    private final TreeMap<String, ConfigReward> cRewards;
    private User user = null;

    public Countdown(LitePlaytimeRewards plugin, Player plyr, TreeMap<String, Reward> rewards, TreeMap<String, ConfigReward> cRewards) {
        this.plugin = plugin;
        this.plyr = plyr;
        this.rewards = rewards;
        this.cRewards = cRewards;
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
        for (Entry<String, Reward> entry : this.rewards.entrySet()) {
            ConfigReward config = cRewards.get(entry.getKey());

            if(config == null){
                this.rewards.remove(entry.getKey());
            }
            
            if (config.getDisabledWorlds().contains(this.plyr.getLocation().getWorld().getName())) {
                break;
            }

            if (config.isCountAfkTime() || !isAfk) {
                this.countDown(entry.getValue());
            }
        }
        this.plugin.checkEligibleForRewards(plyr, this.rewards);
    }

    private void countDown(Reward value) {
        if ((value.getTimeTillNextReward().get(0) - 1200) <= 0) {
            List<Long> newTimes = value.getTimeTillNextReward();
            newTimes.set(0, new Long(0));
            value.setTimeTillNextReward(newTimes);
        } else {
            List<Long> newTimes = value.getTimeTillNextReward();
            newTimes.set(0, newTimes.get(0) - 1200);
            value.setTimeTillNextReward(newTimes);
        }
    }

}
