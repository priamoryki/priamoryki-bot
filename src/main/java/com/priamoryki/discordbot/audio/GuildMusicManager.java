package com.priamoryki.discordbot.audio;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.priamoryki.discordbot.utils.DataSource;
import com.priamoryki.discordbot.utils.PlayerMessage;
import com.priamoryki.discordbot.utils.QueueMessage;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;

import java.util.*;

/**
 * @author Pavel Lymar
 */
public class GuildMusicManager extends AudioEventAdapter {
    private final DataSource data;
    private final Guild guild;
    private final AudioPlayer player;
    private final Deque<AudioTrack> queue;
    private final PlayerSendHandler sendHandler;
    private final PlayerMessage playerMessage;
    private final QueueMessage queueMessage;
    private GuildMusicParameters musicParameters;
    private Timer timer;

    public GuildMusicManager(DataSource data, Guild guild, AudioPlayerManager manager) {
        this.data = data;
        this.guild = guild;
        this.player = manager.createPlayer();
        this.queue = new ArrayDeque<>();
        this.sendHandler = new PlayerSendHandler(player);
        this.playerMessage = new PlayerMessage(this);
        this.queueMessage = new QueueMessage(this);
        this.musicParameters = new GuildMusicParameters();
        player.addListener(this);
    }

    public void setRepeat(boolean repeat) {
        musicParameters.setRepeat(repeat);
    }

    public void reverseRepeat() {
        setRepeat(!musicParameters.getRepeat());
    }

    public PlayerSendHandler getSendHandler() {
        return sendHandler;
    }

    public boolean isPlaying() {
        return player.getPlayingTrack() != null;
    }

    public DataSource getData() {
        return data;
    }

    public Guild getGuild() {
        return guild;
    }

    public List<AudioTrack> getQueue() {
        return new ArrayList<>(queue);
    }

    public AudioPlayer getPlayer() {
        return player;
    }

    public GuildMusicParameters getMusicParameters() {
        return musicParameters;
    }

    public PlayerMessage getPlayerMessage() {
        playerMessage.update();
        return playerMessage;
    }

    public QueueMessage getQueueMessage() {
        queueMessage.update();
        return queueMessage;
    }

    private void startNewDisconnectionTask() {
        long period = 5 * 60_000;
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isPlaying()) {
                    leave(guild.getSelfMember());
                }
            }
        }, period, period);
    }

    public void join(Member member) {
        GuildVoiceState voiceState = Objects.requireNonNull(member).getVoiceState();
        GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inAudioChannel()) {
        } else if (!isPlaying()) {
            guild.getAudioManager().openAudioConnection(voiceState.getChannel());
            startNewDisconnectionTask();
        }
    }

    public void leave(Member member) {
        GuildVoiceState voiceState = Objects.requireNonNull(member).getVoiceState();
        GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        if (voiceState != null && selfVoiceState != null && voiceState.getChannel() == selfVoiceState.getChannel()) {
            // Clears queue and stops playing
            clearQueue();
            guild.getAudioManager().closeAudioConnection();
        }
    }

    public void resume() {
        player.setPaused(false);
    }

    public void pause() {
        player.setPaused(true);
    }

    public void stop() {
        player.stopTrack();
    }

    public void skip() {
        boolean oldRepeat = musicParameters.getRepeat();
        musicParameters.setRepeat(false);
        stop();
        musicParameters.setRepeat(oldRepeat);
    }

    public void seek(long time) {
        if (isPlaying()) {
            player.getPlayingTrack().setPosition(time);
        }
    }

    public void skipTo(int id) {
        List<AudioTrack> list = new ArrayList<>(queue);
        queue.clear();
        queue.addAll(list.subList(Math.max(0, Math.min(id, list.size()) - 1), list.size()));
        skip();
    }

    public void clearQueue() {
        queue.clear();
        skip();
    }

    public void deleteFromQueue(int id) {
        if (id <= queue.size()) {
            List<AudioTrack> list = new ArrayList<>(queue);
            list.remove(id - 1);
            queue.clear();
            queue.addAll(list);
        }
    }

    public void shuffleQueue() {
        List<AudioTrack> list = new ArrayList<>(queue);
        Collections.shuffle(list);
        queue.clear();
        queue.addAll(list);
    }

    private void rebuildFilters() {
        float multiplier = 2;
        float[] BASS_BOOST = {
                0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f
        };
        player.setFilterFactory(
                (audioTrack, audioDataFormat, universalPcmAudioFilter) -> {
                    List<AudioFilter> filters = new ArrayList<>();

                    if (musicParameters.getBassBoost()) {
                        Equalizer equalizer = new Equalizer(audioDataFormat.channelCount, universalPcmAudioFilter);
                        for (int i = 0; i < BASS_BOOST.length; i++) {
                            equalizer.setGain(i, multiplier * BASS_BOOST[i]);
                        }
                        filters.add(equalizer);
                    }

                    TimescalePcmAudioFilter timescale = new TimescalePcmAudioFilter(
                            universalPcmAudioFilter, audioDataFormat.channelCount, audioDataFormat.sampleRate
                    );
                    if (musicParameters.getNightcore()) {
                        timescale.setRate(1.0 + 0.1);
                        filters.add(timescale);
                    }

                    return filters;
                }
        );
    }

    public void bassBoost(boolean value) {
        musicParameters.setBassBoost(value);
        rebuildFilters();
    }

    public void setNightcore(boolean value) {
        musicParameters.setNightcore(value);
        rebuildFilters();
    }

    public void reset() {
        musicParameters = new GuildMusicParameters();
        player.setFilterFactory(null);
    }

    private void startTrack(AudioTrack track, boolean flag) {
        player.startTrack(track, flag);
        playerMessage.startUpdateTask();
    }

    public void queue(AudioTrack track) {
        if (isPlaying()) {
            queue.addLast(track);
        } else {
            startTrack(track, true);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        AudioTrack nextTrack = musicParameters.getRepeat() ? track.makeClone() : queue.poll();
        // If nextTrack is null -> end of playlist
        if (nextTrack == null) {
            playerMessage.endUpdateTask();
            playerMessage.update();
            startNewDisconnectionTask();
        } else {
            startTrack(nextTrack, false);
        }
    }
}
