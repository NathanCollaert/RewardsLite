package com.backtobedrock.rewardslite.domain.notifications;

import com.backtobedrock.rewardslite.utilities.MessageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ChatNotification extends AbstractNotification {
    private final String text;

    public ChatNotification(boolean enabled, String text) {
        super(enabled);
        this.text = text;
    }

    public static ChatNotification deserialize(ConfigurationSection section) {
        boolean cEnabled = section.getBoolean("enabled", false);
        String cText = section.getString("configuration.text", "");
        return new ChatNotification(cEnabled, cText);
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
        player.sendMessage(MessageUtils.replacePlaceholders(this.text, placeholders));
    }
}
