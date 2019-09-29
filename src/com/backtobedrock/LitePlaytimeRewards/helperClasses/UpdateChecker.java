package com.backtobedrock.LitePlaytimeRewards.helperClasses;

import com.backtobedrock.LitePlaytimeRewards.LitePlaytimeRewards;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;

public class UpdateChecker {

    private final LitePlaytimeRewards plugin;
    private final int resourceId;

    public UpdateChecker(LitePlaytimeRewards plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
            try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId).openStream(); Scanner scanner = new Scanner(inputStream)) {
                if (scanner.hasNext()) {
                    consumer.accept(scanner.next());
                }
            } catch (IOException exception) {
                Bukkit.getLogger().log(Level.INFO, "Cannot look for updates: {0}", exception.getMessage());
            }
        });
    }
}
