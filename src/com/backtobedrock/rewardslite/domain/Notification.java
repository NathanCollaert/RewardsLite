package com.backtobedrock.rewardslite.domain;

import com.backtobedrock.rewardslite.domain.enumerations.NotificationType;
import com.backtobedrock.rewardslite.domain.notifications.*;
import com.backtobedrock.rewardslite.utilities.ConfigUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Notification {
    private final ActionBarNotification actionBarNotification;
    private final BossBarNotification bossBarNotification;
    private final ChatNotification chatNotification;
    private final TitleNotification titleNotification;

    public Notification(ActionBarNotification actionBarNotification, BossBarNotification bossBarNotification, ChatNotification chatNotification, TitleNotification titleNotification) {
        this.actionBarNotification = actionBarNotification;
        this.bossBarNotification = bossBarNotification;
        this.chatNotification = chatNotification;
        this.titleNotification = titleNotification;
    }

    public static Notification deserialize(String id, ConfigurationSection section) {
        ActionBarNotification cActionBarNotification = null;
        BossBarNotification cBossBarNotification = null;
        ChatNotification cChatNotification = null;
        TitleNotification cTitleNotification = null;

        for (String e : section.getKeys(false)) {
            NotificationType type = ConfigUtils.getNotificationType(String.format("%s.%s", id, e), e);
            if (type != null) {
                ConfigurationSection notificationSection = section.getConfigurationSection(e);
                if (notificationSection != null) {
                    switch (type) {
                        case CHAT:
                            cChatNotification = ChatNotification.deserialize(notificationSection);
                            break;
                        case BOSSBAR:
                            cBossBarNotification = BossBarNotification.deserialize(id + e, notificationSection);
                            break;
                        case TITLE:
                            cTitleNotification = TitleNotification.deserialize(notificationSection);
                            break;
                        case ACTIONBAR:
                            cActionBarNotification = ActionBarNotification.deserialize(notificationSection);
                            break;
                    }
                }
            }
        }

        return new Notification(cActionBarNotification, cBossBarNotification, cChatNotification, cTitleNotification);
    }

    public Map<String, Object> serialize() {
        HashMap<String, Object> object = new HashMap<>();
        if (getActionBarNotification() != null) {
            object.put("actionbar", getActionBarNotification().serialize());
        }
        if (getBossBarNotification() != null) {
            object.put("bossbar", getBossBarNotification().serialize());
        }
        if (getChatNotification() != null) {
            object.put("chat", getChatNotification().serialize());
        }
        if (getTitleNotification() != null) {
            object.put("title", getTitleNotification().serialize());
        }
        return object;
    }

    public ActionBarNotification getActionBarNotification() {
        return actionBarNotification;
    }

    public BossBarNotification getBossBarNotification() {
        return bossBarNotification;
    }

    public ChatNotification getChatNotification() {
        return chatNotification;
    }

    public TitleNotification getTitleNotification() {
        return titleNotification;
    }

    public List<AbstractNotification> getNotifications() {
        List<AbstractNotification> notifications = new ArrayList<>();
        if (getChatNotification() != null && this.getChatNotification().isEnabled())
            notifications.add(this.getChatNotification());
        if (getTitleNotification() != null && this.getTitleNotification().isEnabled())
            notifications.add(this.getTitleNotification());
        if (getBossBarNotification() != null && this.getBossBarNotification().isEnabled())
            notifications.add(this.getBossBarNotification());
        if (getActionBarNotification() != null && this.getActionBarNotification().isEnabled())
            notifications.add(this.getActionBarNotification());
        return notifications;
    }
}
