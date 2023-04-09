package com.priamoryki.discordbot.api.audio;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.priamoryki.discordbot.api.audio.finder.MusicFinder;
import com.priamoryki.discordbot.api.audio.finder.SpotifySource;
import com.priamoryki.discordbot.api.audio.finder.YandexMusicSource;
import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.utils.DataSource;
import com.priamoryki.discordbot.utils.Utils;
import com.priamoryki.discordbot.utils.messages.PlayerMessage;
import com.priamoryki.discordbot.utils.messages.QueueMessage;
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
    private final AudioPlayerManager audioPlayerManager;
    private final MusicFinder musicFinder;
    private final AudioPlayer player;
    private final Deque<AudioTrack> queue;
    private final PlayerSendHandler sendHandler;
    private final PlayerMessage playerMessage;
    private final QueueMessage queueMessage;
    private GuildMusicParameters musicParameters;
    private Timer timer;

    public GuildMusicManager(DataSource data, Guild guild, AudioPlayerManager audioPlayerManager) {
        this.data = data;
        this.guild = guild;
        this.audioPlayerManager = audioPlayerManager;
        this.musicFinder = new MusicFinder(
                audioPlayerManager,
                new SpotifySource(),
                new YandexMusicSource()
        );
        this.player = audioPlayerManager.createPlayer();
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
        return playerMessage;
    }

    public QueueMessage getQueueMessage() {
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
        clearQueue();
        guild.getAudioManager().closeAudioConnection();
    }

    public void play(SongRequest songRequest) {
        musicFinder.find(songRequest).forEach(track -> queue(track, true));
    }

    public void playNext(SongRequest songRequest) {
        List<AudioTrack> playlist = musicFinder.find(songRequest);
        Collections.reverse(playlist);
        playlist.forEach(track -> queue(track, false));
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

    public void seek(long time) throws CommandException {
        if (!isPlaying()) {
            throw new CommandException("Music is not playing now!");
        }
        player.getPlayingTrack().setPosition(time);
    }

    private void validateId(int id) throws CommandException {
        if (1 > id) {
            throw new CommandException("Id parameter should be natural number!");
        }
        if (id > queue.size()) {
            throw new CommandException("Id parameter should not be more than queue size!");
        }
    }

    public void skipTo(int id) throws CommandException {
        validateId(id);
        List<AudioTrack> list = new ArrayList<>(queue);
        queue.clear();
        queue.addAll(list.subList(id - 1, list.size()));
        skip();
    }

    public void clearQueue() {
        queue.clear();
        skip();
    }

    public void deleteFromQueue(int id) throws CommandException {
        validateId(id);
        List<AudioTrack> list = new ArrayList<>(queue);
        list.remove(id - 1);
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
                    ).setSpeed(musicParameters.getSpeed());
                    if (musicParameters.getNightcore()) {
                        timescale.setRate(1.0 + 0.1);
                    }
                    filters.add(timescale);

                    return filters;
                }
        );
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

    private class SpotifySong implements Consumer<SongRequest> {
        @Override
        public void accept(SongRequest songRequest) {
            try {
                String[] splitted = songRequest.getUrlOrName().split("/");
                Track track = data.getSpotifyApi().getTrack(splitted[splitted.length - 1]).build().execute();
                play(
                        new SongRequest(
                                songRequest.getGuild(),
                                songRequest.getMember(),
                                "scsearch:" + track.getName() + " - " + track.getArtists()[0].getName()
                        )
                );
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.err.println("SpotifySong error: " + e.getMessage());
            }
        }
    }

    private class SpotifyAlbum implements Consumer<SongRequest> {
        @Override
        public void accept(SongRequest songRequest) {
            try {
                String[] splitted = songRequest.getUrlOrName().split("/");
                Album album = data.getSpotifyApi().getAlbum(splitted[splitted.length - 1]).build().execute();
                requestSpotifySongs(
                        songRequest,
                        Arrays.stream(album.getTracks().getItems()).map(TrackSimplified::getUri)
                );
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.err.println("SpotifyAlbum error: " + e.getMessage());
            }
        }
    }

    private class SpotifyPlaylist implements Consumer<SongRequest> {
        @Override
        public void accept(SongRequest songRequest) {
            try {
                String[] splitted = songRequest.getUrlOrName().split("/");
                Playlist playlist = data.getSpotifyApi().getPlaylist(splitted[splitted.length - 1]).build().execute();
                requestSpotifySongs(
                        songRequest,
                        Arrays.stream(playlist.getTracks().getItems()).map(track -> track.getTrack().getUri())
                );
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.err.println("SpotifyPlaylist error: " + e.getMessage());
            }
        }
    }

    private void requestSpotifySongs(SongRequest songRequest, Stream<String> stream) {
        SpotifySong song = new SpotifySong();
        stream.forEach(
                uri -> {
                    String[] splitted = uri.split(":");
                    song.accept(
                            new SongRequest(
                                    songRequest.getGuild(),
                                    songRequest.getMember(),
                                    splitted[splitted.length - 1]
                            )
                    );
                }
        );
    }

    private class YandexMusicSong implements Consumer<SongRequest> {
        @Override
        public void accept(SongRequest songRequest) {
            fromYandexMusic(songRequest, TRACK_TITLE);
        }
    }

    private class YandexMusicAlbum implements Consumer<SongRequest> {
        @Override
        public void accept(SongRequest songRequest) {
            fromYandexMusic(songRequest, TRACKS_TITLES);
        }
    }

    private class YandexMusicPlaylist implements Consumer<SongRequest> {
        @Override
        public void accept(SongRequest songRequest) {
            fromYandexMusic(songRequest, TRACKS_TITLES);
        }
    }

    private void fromYandexMusic(SongRequest songRequest, String className) {
        // TODO not the best implementation
        try {
            Document doc = Jsoup.connect(songRequest.getUrlOrName()).get();
            List<String> tracks = doc.getElementsByClass(className).stream()
                    .map(Element::text).collect(Collectors.toList());
            List<String> artists = doc.getElementsByClass(PLAYLIST_ARTISTS).stream()
                    .map(Element::text).collect(Collectors.toList());
            while (artists.size() < tracks.size()) {
                artists.add(doc.getElementsByClass(ARTIST).get(0).text());
            }
            IntStream.range(0, tracks.size()).forEach(
                    i -> {
                        String artist = artists.get(i);
                        artist = !artist.isEmpty() ? artist : doc.getElementsByClass(ARTIST).get(0).text();
                        play(
                                new SongRequest(
                                        songRequest.getGuild(),
                                        songRequest.getMember(),
                                        "scsearch:" + artist + " - " + tracks.get(i)
                                )
                        );
                    }
            );
        } catch (IOException e) {
            System.err.println("YandexMusic error: " + e.getMessage());
        }
    }
}
