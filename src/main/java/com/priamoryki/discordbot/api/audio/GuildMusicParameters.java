package com.priamoryki.discordbot.api.audio;

/**
 * @author Pavel Lymar
 */
public class GuildMusicParameters {
    private boolean repeat;
    private boolean bassBoost;
    private boolean nightcore;
    private Long cycleStart;
    private Long cycleEnd;
    private double speed = 1d;

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

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isCycled() {
        return cycleStart != null && cycleEnd != null;
    }

    public Long getCycleStart() {
        return cycleStart;
    }

    public void setCycleStart(Long cycleStart) {
        this.cycleStart = cycleStart;
    }

    public Long getCycleEnd() {
        return cycleEnd;
    }

    public void setCycleEnd(Long cycleEnd) {
        this.cycleEnd = cycleEnd;
    }
}
