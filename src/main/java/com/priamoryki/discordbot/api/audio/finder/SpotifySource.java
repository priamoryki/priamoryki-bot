package com.priamoryki.discordbot.api.audio.finder;

import com.priamoryki.discordbot.api.audio.SongRequest;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Pavel Lymar
 */
public class SpotifySource extends CustomAudioSource {
    private final String SPOTIFY_CLIENT_ID_ENV_NAME = "SPOTIFY_CLIENT_ID";
    private final String SPOTIFY_CLIENT_SECRET_ENV_NAME = "SPOTIFY_CLIENT_SECRET";
    private final SpotifyApi spotifyApi;

    public SpotifySource() {
        super();
        patterns.put(
                Pattern.compile("^(https?://)?(www\\.)?open\\.spotify\\.com/track/[^&=%\\?]{22}$"),
                new SpotifySong()
        );
        patterns.put(
                Pattern.compile("^(https?://)?(www\\.)?open\\.spotify\\.com/album/[^&=%\\?]{22}$"),
                new SpotifyAlbum()
        );
        patterns.put(Pattern.compile("^(https?://)?(www\\.)?open\\.spotify\\.com/playlist/[^&=%\\?]{22}$"),
                new SpotifyPlaylist()
        );
        this.spotifyApi = SpotifyApi.builder()
                .setClientId(getSpotifyClientId())
                .setClientSecret(getSpotifyClientSecret())
                .build();
        updateSpotifyApi();
    }

    public String getSpotifyClientId() {
        return System.getenv(SPOTIFY_CLIENT_ID_ENV_NAME);
    }

    public String getSpotifyClientSecret() {
        return System.getenv(SPOTIFY_CLIENT_SECRET_ENV_NAME);
    }

    private void updateSpotifyApi() {
        try {
            spotifyApi.setAccessToken(spotifyApi.clientCredentials().build().execute().getAccessToken());
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            System.err.println("SpotifyApi login error: " + e.getMessage());
        }
    }

    public SpotifyApi getSpotifyApi() {
        // TODO pretty long request
        updateSpotifyApi();
        return spotifyApi;
    }

    private class SpotifySong implements Function<SongRequest, List<SongRequest>> {
        @Override
        public List<SongRequest> apply(SongRequest songRequest) {
            List<SongRequest> result = new ArrayList<>();
            try {
                String[] splitted = songRequest.getUrlOrName().split("/");
                Track track = getSpotifyApi().getTrack(splitted[splitted.length - 1]).build().execute();
                result.add(
                        new SongRequest(
                                songRequest.getGuild(),
                                songRequest.getMember(),
                                "scsearch:" + track.getName() + " - " + track.getArtists()[0].getName()
                        )
                );
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.err.println("SpotifySong error: " + e.getMessage());
            }
            return result;
        }
    }

    private class SpotifyAlbum implements Function<SongRequest, List<SongRequest>> {
        @Override
        public List<SongRequest> apply(SongRequest songRequest) {
            try {
                String[] splitted = songRequest.getUrlOrName().split("/");
                Album album = getSpotifyApi().getAlbum(splitted[splitted.length - 1]).build().execute();
                return requestSpotifySongs(
                        songRequest,
                        Arrays.stream(album.getTracks().getItems()).map(TrackSimplified::getUri)
                );
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.err.println("SpotifyAlbum error: " + e.getMessage());
            }
            return new ArrayList<>();
        }
    }

    private class SpotifyPlaylist implements Function<SongRequest, List<SongRequest>> {
        @Override
        public List<SongRequest> apply(SongRequest songRequest) {
            try {
                String[] splitted = songRequest.getUrlOrName().split("/");
                Playlist playlist = getSpotifyApi().getPlaylist(splitted[splitted.length - 1]).build().execute();
                return requestSpotifySongs(
                        songRequest,
                        Arrays.stream(playlist.getTracks().getItems()).map(track -> track.getTrack().getUri())
                );
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.err.println("SpotifyPlaylist error: " + e.getMessage());
            }
            return new ArrayList<>();
        }
    }

    private List<SongRequest> requestSpotifySongs(SongRequest songRequest, Stream<String> stream) {
        SpotifySong song = new SpotifySong();
        return stream.map(
                uri -> {
                    String[] splitted = uri.split(":");
                    return song.apply(
                            new SongRequest(
                                    songRequest.getGuild(),
                                    songRequest.getMember(),
                                    splitted[splitted.length - 1]
                            )
                    );
                }
        ).flatMap(List::stream).collect(Collectors.toList());
    }
}