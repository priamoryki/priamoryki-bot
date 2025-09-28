package com.priamoryki.discordbot.api.audio;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.priamoryki.discordbot.api.audio.customsources.CustomUserData;
import com.priamoryki.discordbot.api.audio.finder.MusicFinder;
import com.priamoryki.discordbot.api.common.GuildAttributesService;
import com.priamoryki.discordbot.api.messages.HistoryMessage;
import com.priamoryki.discordbot.api.messages.PlayerMessage;
import com.priamoryki.discordbot.api.messages.QueueMessage;
import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.common.Utils;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler.MarkerState.REACHED;

/**
 * @author Pavel Lymar, Michael Ruzavin
 */
public class GuildMusicManager extends AudioEventAdapter {
    private static final float[] BASS_BOOST = {
            0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f
    };
    private final GuildAttributesService guildAttributesService;
    private final MusicFinder musicFinder;
    private final Guild guild;
    private final AudioPlayer player;
    private final Deque<AudioTrack> queue;
    private final PlayerSendHandler sendHandler;
    private final PlayerMessage playerMessage;
    private final QueueMessage queueMessage;
    private final HistoryMessage historyMessage;
    private GuildMusicParameters musicParameters;
    private Timer timer;

    public GuildMusicManager(
            GuildAttributesService guildAttributesService,
            AudioPlayerManager audioPlayerManager,
            MusicFinder musicFinder,
            Guild guild
    ) {
        this.guildAttributesService = guildAttributesService;
        this.musicFinder = musicFinder;
        this.guild = guild;
        this.player = audioPlayerManager.createPlayer();
        this.queue = new ArrayDeque<>();
        this.sendHandler = new PlayerSendHandler(player);
        this.playerMessage = new PlayerMessage(this, guildAttributesService);
        this.queueMessage = new QueueMessage(this, guildAttributesService);
        this.historyMessage = new HistoryMessage(this, guildAttributesService);
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
        return getPlayingTrack() != null;
    }

    public boolean isPaused() {
        return player.isPaused();
    }

    public Guild getGuild() {
        return guild;
    }

    public List<AudioTrack> getQueue() {
        return new ArrayList<>(queue);
    }

    public AudioTrack getPlayingTrack() {
        return player.getPlayingTrack();
    }

    public GuildMusicParameters getMusicParameters() {
        return musicParameters;
    }

    public PlayerMessage getPlayerMessage() {
        return playerMessage;
    }

    public QueueMessage getQueueMessage() {
        return queueMessage;
    }

    public HistoryMessage getHistoryMessage() {
        return historyMessage;
    }

    private void startNewDisconnectionTask() {
        long period = 5 * 60_000L;
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isPlaying()) {
                    return;
                }
                leave(guild.getSelfMember());
            }
        }, period, period);
    }

    public void join(Member member) throws CommandException {
        GuildVoiceState voiceState = member.getVoiceState();
        GuildVoiceState selfVoiceState = guild.getSelfMember().getVoiceState();
        if (voiceState == null || !voiceState.inAudioChannel()) {
            throw new CommandException("You are not in the voice channel!");
        }
        guild.getAudioManager().openAudioConnection(voiceState.getChannel());
        startNewDisconnectionTask();
    }

    public void leave(Member member) {
        // Clears queue and stops playing
        clearQueue(member);
        guild.getAudioManager().closeAudioConnection();
    }

    public void play(SongRequest songRequest) {
        var result = musicFinder.find(songRequest);
        List<AudioTrack> playlist = result.loadedTracks();
        playlist.forEach(track -> queue(track, true));

        MessageChannel channel = guildAttributesService.getOrCreateMainChannel(guild);
        result.exceptions().forEach(e -> channel.sendMessage(e.getMessage()).queue());
    }

    public void playNext(SongRequest songRequest) {
        var result = musicFinder.find(songRequest);
        List<AudioTrack> playlist = result.loadedTracks();
        Collections.reverse(playlist);
        playlist.forEach(track -> queue(track, false));

        MessageChannel channel = guildAttributesService.getOrCreateMainChannel(guild);
        result.exceptions().forEach(e -> channel.sendMessage(e.getMessage()).queue());
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

    public void skip(Member member) {
        if (!isPlaying()) {
            return;
        }
        getPlayingTrack().getUserData(CustomUserData.class).setSkippedBy(member.getUser());
        boolean oldRepeat = musicParameters.getRepeat();
        musicParameters.setRepeat(false);
        stop();
        musicParameters.setRepeat(oldRepeat);
    }

    public void seek(long time) throws CommandException {
        if (!isPlaying()) {
            throw new CommandException("Music is not playing now!");
        }
        getPlayingTrack().setPosition(time);
    }

    public void skipTo(Member member, int id) throws CommandException {
        Utils.validateId(id, queue.size());
        List<AudioTrack> list = new ArrayList<>(queue);
        queue.clear();
        queue.addAll(list.subList(id - 1, list.size()));
        skip(member);
    }

    public void clearQueue(Member member) {
        queue.clear();
        skip(member);
    }

    public void deleteFromQueue(int from, int to) throws CommandException {
        Utils.validateBounds(from, to, queue.size(), "Can't remove interval that isn't in queue!");
        List<AudioTrack> list = new ArrayList<>(queue);
        list.subList(from - 1, to).clear();
        queue.clear();
        queue.addAll(list);
    }

    public void shuffleQueue() {
        List<AudioTrack> list = new ArrayList<>(queue);
        Collections.shuffle(list);
        queue.clear();
        queue.addAll(list);
    }

    private void rebuildFilters() {
        float multiplier = 1;
        player.setFilterFactory((audioTrack, audioDataFormat, universalPcmAudioFilter) -> {
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
            ).setSpeed(musicParameters.getSpeed());
            if (musicParameters.getNightcore()) {
                timescale.setRate(1.0 + 0.1);
            }
            filters.add(timescale);

            return filters;
        });
    }

    public void setSpeed(double speed) {
        musicParameters.setSpeed(speed);
        rebuildFilters();
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
        resume();
        playerMessage.startUpdateTask();
    }

    private void queue(AudioTrack track, boolean asLast) {
        if (isPlaying()) {
            if (asLast) {
                queue.addLast(track);
            } else {
                queue.addFirst(track);
            }
        } else {
            startTrack(track, false);
        }
    }

    public void cycle(long start, long finish) {
        musicParameters.setCycleStart(start);
        musicParameters.setCycleEnd(finish);
        AudioTrack track = getPlayingTrack();
        track.setPosition(start);
        track.setMarker(new TrackMarker(finish, markerState -> {
            if (markerState == REACHED) {
                cycle(start, finish);
            }
        }));
    }

    public void uncycle() {
        AudioTrack track = getPlayingTrack();
        track.setMarker(null);
        musicParameters.setCycleStart(null);
        musicParameters.setCycleEnd(null);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        historyMessage.put(track);
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

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        MessageChannel channel = guildAttributesService.getOrCreateMainChannel(guild);
        channel.sendMessage("Error on track " + Utils.audioTrackToString(track) + " occurred: " + exception.getMessage()).queue();
    }
}
