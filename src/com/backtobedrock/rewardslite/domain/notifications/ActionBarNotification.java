package com.backtobedrock.rewardslite.domain.notifications;

import com.backtobedrock.rewardslite.domain.enumerations.MinecraftVersion;
import com.backtobedrock.rewardslite.utilities.MessageUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ActionBarNotification extends AbstractNotification {
    private final String text;

    public ActionBarNotification(boolean enabled, String text) {
        super(enabled);
        this.text = text;
    }

    public static ActionBarNotification deserialize(ConfigurationSection section) {
        MinecraftVersion minecraftVersion = MinecraftVersion.get();
        boolean cEnabled = minecraftVersion != null && minecraftVersion.greaterThanOrEqualTo(MinecraftVersion.v1_10) && section.getBoolean("enabled", false);
        String cText = section.getString("configuration.text", "");
        return new ActionBarNotification(cEnabled, cText);
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> object = new HashMap<>();
        HashMap<String, Object> configuration = new HashMap<>();
        configuration.put("text", getText());
        object.put("enabled", this.isEnabled());
        object.put("configuration", configuration);
        return object;
    }

    public String getText() {
        return text;
    }

    @Override
    public void notify(Player player) {
        Map<String, String> placeholders = new HashMap<String, String>() {{
            put("player", player.getName());
        }};
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(MessageUtils.replacePlaceholders(this.text, placeholders)));
    }
}
