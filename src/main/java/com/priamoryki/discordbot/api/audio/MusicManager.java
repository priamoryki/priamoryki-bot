package com.priamoryki.discordbot.api.audio;

import com.priamoryki.discordbot.utils.DataSource;
import com.priamoryki.discordbot.utils.Utils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.apache.hc.core5.http.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Pavel Lymar
 */
public class MusicManager {
    private static final String ARTIST = "d-artists";
    private static final String PLAYLIST_ARTISTS = "d-track__artists";
    private static final String TRACK_TITLE = "sidebar-track__title";
    private static final String TRACKS_TITLES = "d-track__name";
    private final DataSource data;
    private final Map<Long, GuildMusicManager> managers;
    private final AudioPlayerManager audioPlayerManager;
    private final Map<String, Consumer<SongRequest>> patterns;

    public MusicManager(DataSource data) {
        this.data = data;
        this.managers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.patterns = new HashMap<>();
        patterns.put("^(https?://)?(www\\.)?open\\.spotify\\.com/track/[^&=%\\?]{22}", new SpotifySong());
        patterns.put("^(https?://)?(www\\.)?open\\.spotify\\.com/album/[^&=%\\?]{22}", new SpotifyAlbum());
        patterns.put("^(https?://)?(www\\.)?open\\.spotify\\.com/playlist/[^&=%\\?]{22}", new SpotifyPlaylist());
        patterns.put("^(https?://)?(www\\.)?music\\.yandex\\.[a-z]+/album/[0-9]+/track/[0-9]+", new YandexMusicSong());
        patterns.put("^(https?://)?(www\\.)?music\\.yandex\\.[a-z]+/album/[0-9]+", new YandexMusicAlbum());
        patterns.put("^(https?://)?(www\\.)?music\\.yandex\\.[a-z]+/users/.+/playlists/[0-9]+", new YandexMusicPlaylist());

        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return managers.computeIfAbsent(
                guild.getIdLong(),
                guildId -> {
                    GuildMusicManager guildMusicManager = new GuildMusicManager(data, guild, audioPlayerManager);
                    guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
                    return guildMusicManager;
                }
        );
    }

    public void play(SongRequest songRequest) {
        Guild guild = songRequest.getGuild();
        Member member = songRequest.getMember();
        String urlOrName = songRequest.getUrlOrName();
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        guildMusicManager.join(member);

        audioPlayerManager.loadItemOrdered(guildMusicManager, urlOrName, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                track.setUserData(member.getUser());
                guildMusicManager.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (Utils.isUrl(urlOrName)) {
                    playlist.getTracks().forEach(this::trackLoaded);
                } else {
                    trackLoaded(playlist.getTracks().get(0));
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {
                for (String pattern : patterns.keySet()) {
                    if (urlOrName.matches(pattern)) {
                        patterns.get(pattern).accept(songRequest);
                        break;
                    }
                }
            }
        });
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
                                "ytsearch:" + track.getName() + " - " + track.getArtists()[0].getName()
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
        try {
            Document doc = Jsoup.connect(songRequest.getUrlOrName()).get();
            List<String> tracks = doc.getElementsByClass(className).stream()
                    .map(Element::text).collect(Collectors.toList());
            List<String> artists = doc.getElementsByClass(PLAYLIST_ARTISTS).stream()
                    .map(Element::text).collect(Collectors.toList());
            while (artists.size() > tracks.size()) {
                artists.add(doc.getElementsByClass(ARTIST).get(0).text());
            }
            IntStream.range(0, tracks.size()).forEach(
                    i -> play(
                            new SongRequest(
                                    songRequest.getGuild(),
                                    songRequest.getMember(),
                                    "ytsearch:" + artists.get(i) + " - " + tracks.get(i)
                            )
                    )
            );
        } catch (IOException e) {
            System.err.println("YandexMusic error: " + e.getMessage());
        }
    }
}
