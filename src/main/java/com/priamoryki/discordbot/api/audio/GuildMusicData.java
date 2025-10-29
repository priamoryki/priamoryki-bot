package com.priamoryki.discordbot.api.audio;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.priamoryki.discordbot.api.audio.customsources.CustomUserData;
import com.priamoryki.discordbot.common.Utils;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * @author Pavel Lymar
 */
public class GuildMusicData {
    private static final float[] BASS_BOOST = {
            0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f
    };
    private final Guild guild;
    private final AudioPlayer player;
    private final Deque<AudioTrack> queue;
    private final Deque<AudioTrack> history;
    private boolean repeat;
    private boolean bassBoost;
    private boolean nightcore;
    private Long cycleStart;
    private Long cycleEnd;
    private double speed;

    public GuildMusicData(Guild guild, AudioPlayer player) {
        this.guild = guild;
        this.player = player;
        this.queue = new ArrayDeque<>();
        this.history = new ArrayDeque<>();
        setDefaults();
    }

    public Guild getGuild() {
        return guild;
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public boolean isPlaying() {
        return getPlayingTrack() != null;
    }

    public boolean isPaused() {
        return player.isPaused();
    }

    public AudioTrack getPlayingTrack() {
        return player.getPlayingTrack();
    }

    Deque<AudioTrack> getQueue() {
        return queue;
    }

    public List<AudioTrack> getQueueCopy() {
        return new ArrayList<>(queue);
    }

    public List<AudioTrack> getHistoryCopy() {
        return new ArrayList<>(history);
    }

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
        rebuildFilters();
    }

    public boolean getNightcore() {
        return nightcore;
    }

    public void setNightcore(boolean nightcore) {
        this.nightcore = nightcore;
        rebuildFilters();
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
        rebuildFilters();
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

    public void onTrackEnd(AudioTrack track, AudioTrack nextTrack) {
        if (Utils.tacksEquals(track, nextTrack)) {
            track.getUserData(CustomUserData.class).increaseTimesPlayed();
        } else {
            history.addFirst(track);
        }
    }

    public void setDefaults() {
        this.repeat = false;
        this.bassBoost = false;
        this.nightcore = false;
        this.cycleStart = null;
        this.cycleEnd = null;
        this.speed = 1d;
        rebuildFilters();
    }

    private void rebuildFilters() {
        player.setFilterFactory((audioTrack, audioDataFormat, universalPcmAudioFilter) -> {
            List<AudioFilter> filters = new ArrayList<>();

            if (bassBoost) {
                float multiplier = 1;
                Equalizer equalizer = new Equalizer(audioDataFormat.channelCount, universalPcmAudioFilter);
                for (int i = 0; i < BASS_BOOST.length; i++) {
                    equalizer.setGain(i, multiplier * BASS_BOOST[i]);
                }
                filters.add(equalizer);
            }

            TimescalePcmAudioFilter timescale = new TimescalePcmAudioFilter(
                    universalPcmAudioFilter, audioDataFormat.channelCount, audioDataFormat.sampleRate
            ).setSpeed(speed);
            if (nightcore) {
                timescale.setRate(1.0 + 0.1);
            }
            filters.add(timescale);

            return filters;
        });
    }
}
