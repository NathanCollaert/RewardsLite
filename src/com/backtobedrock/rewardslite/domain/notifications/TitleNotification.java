package com.backtobedrock.rewardslite.domain.notifications;

import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TitleNotification extends AbstractNotification {
    private final String title;
    private final String subTitle;

    public TitleNotification(boolean enabled, String title, String subTitle) {
        super(enabled);
        this.title = title;
        this.subTitle = subTitle;
    }

    public static TitleNotification deserialize(ConfigurationSection section) {
        MinecraftVersion minecraftVersion = MinecraftVersion.get();
        boolean cEnabled = minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_11) && section.getBoolean("enabled", false);
        String cTitle = section.getString("configuration.title", "");
        String cSubTitle = section.getString("configuration.subtitle", "");
        return new TitleNotification(cEnabled, cTitle, cSubTitle);
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> object = new HashMap<>();
        HashMap<String, Object> configuration = new HashMap<>();
        configuration.put("title", getTitle());
        configuration.put("subtitle", getSubTitle());
        object.put("enabled", this.isEnabled());
        object.put("configuration", configuration);
        return object;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    @Override
    public void notify(Player player) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("player", player.getName());
        }};
        player.sendTitle(MessageUtils.replacePlaceholders(this.title, placeholders), MessageUtils.replacePlaceholders(this.subTitle, placeholders), 10, 70, 20);
    }
}
