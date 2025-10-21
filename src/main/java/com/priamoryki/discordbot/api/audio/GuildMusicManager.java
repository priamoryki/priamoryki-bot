package com.priamoryki.discordbot.api.audio;

import com.priamoryki.discordbot.api.audio.customsources.CustomUserData;
import com.priamoryki.discordbot.api.audio.finder.MusicFinder;
import com.priamoryki.discordbot.api.common.ExceptionNotifier;
import com.priamoryki.discordbot.api.common.GuildAttributesService;
import com.priamoryki.discordbot.api.messages.HistoryMessage;
import com.priamoryki.discordbot.api.messages.PlayerMessage;
import com.priamoryki.discordbot.api.messages.QueueMessage;
import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.common.Utils;
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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import static com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler.MarkerState.REACHED;

/**
 * @author Pavel Lymar, Michael Ruzavin
 */
@Component
@Scope("prototype")
public class GuildMusicManager extends AudioEventAdapter {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GuildAttributesService guildAttributesService;
    private final MusicFinder musicFinder;
    private final ExceptionNotifier exceptionNotifier;
    private final Guild guild;
    private final PlayerMessage playerMessage;
    private final QueueMessage queueMessage;
    private final HistoryMessage historyMessage;
    private final GuildMusicData musicData;
    private Timer timer;

    public GuildMusicManager(
            GuildAttributesService guildAttributesService,
            AudioPlayerManager audioPlayerManager,
            MusicFinder musicFinder,
            ExceptionNotifier exceptionNotifier,
            Guild guild
    ) {
        AudioPlayer player = audioPlayerManager.createPlayer();
        this.guildAttributesService = guildAttributesService;
        this.musicFinder = musicFinder;
        this.exceptionNotifier = exceptionNotifier;
        this.guild = guild;
        this.musicData = new GuildMusicData(guild, player);
        this.playerMessage = new PlayerMessage(this, musicData, guildAttributesService);
        this.queueMessage = new QueueMessage(musicData, guildAttributesService);
        this.historyMessage = new HistoryMessage(musicData, guildAttributesService);

        musicData.getPlayer().addListener(this);
        PlayerSendHandler sendHandler = new PlayerSendHandler(player);
        guild.getAudioManager().setSendingHandler(sendHandler);
    }

    public void setRepeat(boolean repeat) {
        musicData.setRepeat(repeat);
    }

    public void reverseRepeat() {
        setRepeat(!musicData.getRepeat());
    }

    public GuildMusicData getMusicData() {
        return musicData;
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
                if (musicData.isPlaying()) {
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
            throw new CommandException(member.getAsMention() + " You are not in the voice channel!");
        }
        guild.getAudioManager().openAudioConnection(voiceState.getChannel());
        startNewDisconnectionTask();
    }

    public void leave(Member member) {
        // Clears queue and stops playing
        clearQueue(member);
        guild.getAudioManager().closeAudioConnection();
    }

    public void play(SongRequest songRequest) throws CommandException {
        var result = musicFinder.find(songRequest);
        List<AudioTrack> playlist = result.loadedTracks();
        List<Exception> exceptions = result.exceptions();
        playlist.forEach(track -> queue(track, true));

        if (!exceptions.isEmpty()) {
            throw new CommandException(result.exceptions().stream().map(Throwable::getMessage).collect(Collectors.joining("\n")));
        }
    }

    public void playNext(SongRequest songRequest) throws CommandException {
        var result = musicFinder.find(songRequest);
        List<AudioTrack> playlist = result.loadedTracks();
        List<Exception> exceptions = result.exceptions();
        Utils.getReversedList(playlist).forEach(track -> queue(track, false));

        if (!exceptions.isEmpty()) {
            throw new CommandException(result.exceptions().stream().map(Throwable::getMessage).collect(Collectors.joining("\n")));
        }
    }

    public void resume() {
        musicData.getPlayer().setPaused(false);
    }

    public void pause() {
        musicData.getPlayer().setPaused(true);
    }

    public void stop() {
        musicData.getPlayer().stopTrack();
    }

    public void skip(User user) {
        if (!musicData.isPlaying()) {
            return;
        }
        musicData.getPlayingTrack().getUserData(CustomUserData.class).setSkippedBy(user);
        boolean oldRepeat = musicData.getRepeat();
        musicData.setRepeat(false);
        stop();
        musicData.setRepeat(oldRepeat);
    }

    public void seek(long time) throws CommandException {
        if (!musicData.isPlaying()) {
            throw new CommandException("Music is not playing now!");
        }
        musicData.getPlayingTrack().setPosition(time);
    }

    public void skipTo(Member member, int id) throws CommandException {
        var queue = musicData.getQueue();
        Utils.validateId(id, queue.size());
        List<AudioTrack> list = new ArrayList<>(queue);
        queue.clear();
        queue.addAll(list.subList(id - 1, list.size()));
        skip(member.getUser());
    }

    public void clearQueue(Member member) {
        var queue = musicData.getQueue();
        queue.clear();
        skip(member.getUser());
    }

    public void deleteFromQueue(int from, int to) throws CommandException {
        var queue = musicData.getQueue();
        Utils.validateBounds(from, to, queue.size(), "Can't remove interval that isn't in queue!");
        List<AudioTrack> list = new ArrayList<>(queue);
        list.subList(from - 1, to).clear();
        queue.clear();
        queue.addAll(list);
    }

    public void shuffleQueue() {
        var queue = musicData.getQueue();
        List<AudioTrack> list = new ArrayList<>(queue);
        Collections.shuffle(list);
        queue.clear();
        queue.addAll(list);
    }

    public void setSpeed(double speed) {
        musicData.setSpeed(speed);
    }

    public void bassBoost(boolean value) {
        musicData.setBassBoost(value);
    }

    public void setNightcore(boolean value) {
        musicData.setNightcore(value);
    }

    public void reset() {
        musicData.setDefaults();
    }

    private void startTrack(AudioTrack track, boolean flag) {
        musicData.getPlayer().startTrack(track, flag);
        resume();
        playerMessage.startUpdateTask();
    }

    private void queue(AudioTrack track, boolean asLast) {
        var queue = musicData.getQueue();
        if (musicData.isPlaying()) {
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
        musicData.setCycleStart(start);
        musicData.setCycleEnd(finish);
        AudioTrack track = musicData.getPlayingTrack();
        track.setMarker(new TrackMarker(finish, markerState -> {
            if (markerState == REACHED) {
                cycle(start, finish);
            }
        }));
        track.setPosition(start);
    }

    public void uncycle() {
        AudioTrack track = musicData.getPlayingTrack();
        track.setMarker(null);
        musicData.setCycleStart(null);
        musicData.setCycleEnd(null);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        musicData.onTrackStart(track);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        var queue = musicData.getQueue();
        AudioTrack nextTrack = musicData.getRepeat() ? track.makeClone() : queue.poll();
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
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException e) {
        logger.error("Track Exception", e);
        MessageChannel channel = guildAttributesService.getOrCreateMainChannel(guild);
        channel.sendMessage("Error on track " + Utils.audioTrackToString(track) + " occurred: " + e.getMessage()).queue();
        exceptionNotifier.notify(e);
        skip(null);
    }
}
