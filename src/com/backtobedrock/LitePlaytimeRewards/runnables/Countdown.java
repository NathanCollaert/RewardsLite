package com.backtobedrock.LitePlaytimeRewards.runnables;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsCRUD;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.Reward;
import com.earth2me.essentials.User;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Countdown extends BukkitRunnable {

    private final long loopTimer;
    private int saveCounter = 0;
    private boolean givenReward = false;

    LitePlaytimeRewardsCRUD crud;

    private final LitePlaytimeRewards plugin;
    private final Player plyr;
    private TreeMap<String, Reward> rewards = new TreeMap<>();
    private User user = null;

    public Countdown(long looptimer, Player plyr) {
        this.plugin = LitePlaytimeRewards.getInstance();
        this.loopTimer = looptimer;
        this.plyr = plyr;
        this.crud = this.plugin.getFromCRUDCache(plyr.getUniqueId());
        this.rewards = this.crud.getRewards();
        if (plugin.ess != null) {
            this.user = plugin.ess.getUser(plyr);
        }
    }

    @Override
    public void run() {
        long playtime = this.crud.getPlaytime(), afktime = this.crud.getAfktime();

        //Check if user is afk and increment play/afk-time
        if (this.user == null) {
            this.checkRewards(false);
            //add playtime
            this.crud.setPlaytime(playtime + this.loopTimer, false);
        } else {
            this.checkRewards(this.user.isAfk());
            if (this.user.isAfk()) {
                //add afktime
                this.crud.setAfktime(afktime + this.loopTimer, false);
            } else {
                //add playtime
                this.crud.setPlaytime(playtime + this.loopTimer, false);
            }
        }

        //Count up save counter
        this.saveCounter += this.loopTimer;

        //save data if autosave is met or reward has been given
        if (this.saveCounter >= this.plugin.getLPRConfig().getAutoSave() || this.givenReward) {
            this.crud.saveConfig();
            this.saveCounter = 0;
            this.givenReward = false;
        }
    }

    private void checkRewards(boolean isAfk) {
        this.rewards.entrySet().stream()
                .filter((Map.Entry<String, Reward> r) -> r.getValue().getAmountPending() > 0
                || ((!r.getValue().getcReward().isUsePermission() || this.plyr.hasPermission("liteplaytimerewards.reward." + r.getKey()))
                && (!r.getValue().getTimeTillNextReward().get(0).equals(-1L))
                && !r.getValue().getcReward().getDisabledWorlds().contains(this.plyr.getLocation().getWorld().getName().toLowerCase())
                && (r.getValue().getcReward().isCountAfkTime() || !isAfk)))
                .forEach((entry) -> {
                    this.countDown(entry.getValue());
                });
    }

    private void countDown(Reward value) {
        Long timeNeededNew = !value.getTimeTillNextReward().get(0).equals(-1L) && value.getTimeTillNextReward().get(0) - this.loopTimer <= 0L
                ? 0L
                : value.getTimeTillNextReward().get(0).equals(-1L)
                ? value.getTimeTillNextReward().get(0)
                : value.getTimeTillNextReward().get(0) - this.loopTimer;
        if (timeNeededNew.equals(0L)) {
            value.setAmountPending(this.plugin.giveReward(value, this.plyr, true, 1));
            value.removeFirstTimeTillNextReward();
            this.givenReward = true;
        } else {
            if (value.getAmountPending() > 0) {
                int oldPending = value.getAmountPending();
                value.setAmountPending(this.plugin.giveReward(value, this.plyr, true, 0));
                if (oldPending != value.getAmountPending()) {
                    this.givenReward = true;
                }
            }
            if (timeNeededNew > 0L) {
                value.setFirstTimeTillNextReward(timeNeededNew);
            }
        }
    }
}
