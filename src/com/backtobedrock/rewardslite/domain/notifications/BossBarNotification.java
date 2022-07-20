package com.backtobedrock.rewardslite.domain.notifications;

import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import com.backtobedrock.rewardslite.runnables.BossBarNotificationTask;
import com.backtobedrock.rewardslite.utilities.ConfigUtils;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BossBarNotification extends AbstractNotification {
    private final String text;
    private final BarColor color;
    private final BarStyle style;

    public BossBarNotification(boolean enabled, String text, BarColor color, BarStyle style) {
        super(enabled);
        this.text = text;
        this.color = color;
        this.style = style;
    }

    public static BossBarNotification deserialize(String id, ConfigurationSection section) {
        MinecraftVersion minecraftVersion = MinecraftVersion.get();
        if (minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_9)) {
            boolean cEnabled = section.getBoolean("enabled", false);
            String cText = section.getString("configuration.text", "");
            BarColor cColor = ConfigUtils.getBarColor(id + "configuration.color", section.getString("configuration.color", "red"), BarColor.RED);
            BarStyle cStyle = ConfigUtils.getBarStyle(id + "configuration.style", section.getString("configuration.style", "solid"), BarStyle.SOLID);

            return new BossBarNotification(cEnabled, cText, cColor, cStyle);
        }
        return null;
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> object = new HashMap<>();
        HashMap<String, Object> configuration = new HashMap<>();
        configuration.put("text", getText());
        configuration.put("color", getColor().toString());
        configuration.put("style", getStyle().toString());
        object.put("enabled", this.isEnabled());
        object.put("configuration", configuration);
        return object;
    }

    public String getText() {
        return text;
    }

    public BarColor getColor() {
        return color;
    }

    public BarStyle getStyle() {
        return style;
    }

    @Override
    public void notify(Player player) {
        new BossBarNotificationTask(player, this).start();
    }
}
