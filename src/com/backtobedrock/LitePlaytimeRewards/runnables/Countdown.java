package com.backtobedrock.LitePlaytimeRewards.runnables;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewardsCRUD;
import com.backtobedrock.LitePlaytimeRewards.helperClasses.Reward;
import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class Countdown extends BukkitRunnable {

    private final long loopTimer;
    private int saveCounter = 0;
    private boolean givenReward = false;
    private final List<String> removeRewards = new ArrayList<>();

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
        this.removeRewards.clear();
        this.rewards.entrySet().stream()
                .filter((Map.Entry<String, Reward> r) -> r.getValue().getAmountPending() > 0
                || ((!r.getValue().getcReward().isUsePermission() || this.plyr.hasPermission("liteplaytimerewards.reward." + r.getKey()))
                && (!r.getValue().getTimeTillNextReward().get(0).equals(-1L))
                && !r.getValue().getcReward().getDisabledWorlds().contains(this.plyr.getLocation().getWorld().getName().toLowerCase())
                && (r.getValue().getcReward().isCountAfkTime() || !isAfk)))
                .forEach((entry) -> {
                    this.countDown(entry.getKey(), entry.getValue());
                });

        this.crud.replaceRewards(this.rewards, false);

        this.removeRewards.forEach(e -> this.rewards.remove(e));
    }

    private void countDown(String name, Reward value) {
        Long timeNeededNew = !value.getTimeTillNextReward().get(0).equals(-1L) && value.getTimeTillNextReward().get(0) - this.loopTimer < 0
                ? 0
                : value.getTimeTillNextReward().get(0) - this.loopTimer;
        if (timeNeededNew == 0L) {
            value.setAmountPending(this.plugin.giveReward(value, this.plyr, true, 1));
            value.getTimeTillNextReward().remove(0);
            if (value.getTimeTillNextReward().isEmpty()) {
                if (value.getcReward().isLoop()) {
                    value.setTimeTillNextReward(new ArrayList(value.getcReward().getPlaytimeNeeded()));
                } else {
                    value.getTimeTillNextReward().add(-1L);
                    if (value.getAmountPending() == 0) {
                        this.removeRewards.add(name);
                    }
                }
            }
            this.givenReward = true;
        } else {
            if (value.getAmountPending() > 0) {
                value.setAmountPending(this.plugin.giveReward(value, this.plyr, true, 0));
                this.givenReward = true;
            }
            if (timeNeededNew < -1L) {
                value.getTimeTillNextReward().set(0, -1L);
                if (value.getAmountPending() == 0) {
                    this.removeRewards.add(name);
                }
            } else {
                List<Long> newTimes = value.getTimeTillNextReward();
                newTimes.set(0, timeNeededNew);
                value.setTimeTillNextReward(newTimes);
            }
        }
    }
}
