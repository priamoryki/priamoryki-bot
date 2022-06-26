package com.priamoryki.discordbot.audio;

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
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Pavel Lymar
 */
public class MusicManager {
    private final DataSource data;
    private final Map<Long, GuildMusicManager> managers;
    private final AudioPlayerManager audioPlayerManager;
    private final Map<String, Function<SongRequest, Void>> patterns;

    public MusicManager(DataSource data) {
        this.data = data;
        this.managers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.patterns = new HashMap<>();
        patterns.put("^(https?://)?(www\\.)?open\\.spotify\\.com/track/[^&=%\\?]{22}$", new SpotifySong());
        patterns.put("^(https?://)?(www\\.)?open\\.spotify\\.com/album/[^&=%\\?]{22}$", new SpotifyAlbum());
        patterns.put("^(https?://)?(www\\.)?open\\.spotify\\.com/playlist/[^&=%\\?]{22}$", new SpotifyPlaylist());

        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return managers.computeIfAbsent(guild.getIdLong(),
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
                String[] splitted = urlOrName.split("/");
                for (String pattern : patterns.keySet()) {
                    if (urlOrName.matches(pattern)) {
                        patterns.get(pattern).apply(
                                new SongRequest(
                                        songRequest.getGuild(),
                                        songRequest.getMember(),
                                        splitted[splitted.length - 1]
                                )
                        );
                        break;
                    }
                }
            }
        });
    }

    private void requestSongs(SongRequest songRequest, Stream<String> stream) {
        SpotifySong song = new SpotifySong();
        stream.forEach(
                uri -> {
                    String[] splitted = uri.split(":");
                    song.apply(
                            new SongRequest(
                                    songRequest.getGuild(),
                                    songRequest.getMember(),
                                    splitted[splitted.length - 1]
                            )
                    );
                }
        );
    }

    private class SpotifySong implements Function<SongRequest, Void> {
        @Override
        public Void apply(SongRequest songRequest) {
            try {
                Track track = data.getSpotifyApi().getTrack(songRequest.getUrlOrName()).build().execute();
                System.out.println(track.getName());
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
            return null;
        }
    }

    private class SpotifyAlbum implements Function<SongRequest, Void> {
        @Override
        public Void apply(SongRequest songRequest) {
            try {
                Album album = data.getSpotifyApi().getAlbum(songRequest.getUrlOrName()).build().execute();
                requestSongs(
                        songRequest,
                        Arrays.stream(album.getTracks().getItems()).map(TrackSimplified::getUri)
                );
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.err.println("SpotifyAlbum error: " + e.getMessage());
            }
            return null;
        }
    }

    private class SpotifyPlaylist implements Function<SongRequest, Void> {
        @Override
        public Void apply(SongRequest songRequest) {
            try {
                Playlist playlist = data.getSpotifyApi().getPlaylist(songRequest.getUrlOrName()).build().execute();
                requestSongs(
                        songRequest,
                        Arrays.stream(playlist.getTracks().getItems()).map(track -> track.getTrack().getUri())
                );
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.err.println("SpotifyPlaylist error: " + e.getMessage());
            }
            return null;
        }
    }
}
