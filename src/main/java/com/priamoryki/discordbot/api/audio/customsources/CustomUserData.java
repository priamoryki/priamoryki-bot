package com.priamoryki.discordbot.api.audio.customsources;

import net.dv8tion.jda.api.entities.User;

/**
 * @author Pavel Lymar
 */
public class CustomUserData {
    private User queuedBy;
    private User skippedBy;

    public CustomUserData() {
    }

    public User getQueuedBy() {
        return queuedBy;
    }

    public void setQueuedBy(User queuedBy) {
        this.queuedBy = queuedBy;
    }

    public User getSkippedBy() {
        return skippedBy;
    }

    public void setSkippedBy(User skippedBy) {
        this.skippedBy = skippedBy;
    }
}
