package com.priamoryki.discordbot.api.audio;

/**
 * @author Pavel Lymar
 */
public class GuildMusicParameters {
    private boolean repeat;
    private boolean bassBoost;
    private boolean nightcore;

    public boolean getRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean getBassBoost() {
        return bassBoost;
    }

    public void setBassBoost(boolean bassBoost) {
        this.bassBoost = bassBoost;
    }

    public boolean getNightcore() {
        return nightcore;
    }

    public void setNightcore(boolean nightcore) {
        this.nightcore = nightcore;
    }
}
