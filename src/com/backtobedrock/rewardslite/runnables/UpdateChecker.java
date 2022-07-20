package com.backtobedrock.rewardslite.runnables;

import com.backtobedrock.rewardslite.Rewardslite;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.logging.Level;

public class UpdateChecker extends BukkitRunnable {

    private final Rewardslite plugin = JavaPlugin.getPlugin(Rewardslite.class);
    private boolean outdated = false;
    private String newestVersion;

    public void start() {
        this.runTaskTimerAsynchronously(this.plugin, 0L, 12000L);
    }

    public void stop() {
        this.cancel();
    }

    public boolean isOutdated() {
        return outdated;
    }

    public String getNewestVersion() {
        return newestVersion;
    }

    public void check() {
        try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + 71784).openStream(); Scanner scanner = new Scanner(inputStream)) {
            if (scanner.hasNext()) {
                this.newestVersion = scanner.next();
                this.outdated = !this.plugin.getDescription().getVersion().equalsIgnoreCase(this.newestVersion);
            }
        } catch (IOException exception) {
            Bukkit.getLogger().log(Level.INFO, "Cannot look for updates: {0}", exception.getMessage());
        }
    }

    @Override
    public void run() {
        this.check();
    }
}
