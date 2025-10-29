package com.priamoryki.discordbot.api.audio.customsources;

import net.dv8tion.jda.api.entities.User;

import java.util.Objects;
import java.util.UUID;

/**
 * @author Pavel Lymar
 */
public class CustomUserData {
    private final UUID uuid;
    private User queuedBy;
    private User skippedBy;
    private int timesPlayed;

    public CustomUserData() {
        this.uuid = UUID.randomUUID();
    }

    public UUID getUuid() {
        return uuid;
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

    public int getTimesPlayed() {
        return timesPlayed;
    }

    public void increaseTimesPlayed() {
        this.timesPlayed++;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CustomUserData userData)) {
            return false;
        }
        return Objects.equals(uuid, userData.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
