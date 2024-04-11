package com.priamoryki.discordbot.api.audio.customsources.spotify;

import com.priamoryki.discordbot.api.audio.customsources.ProxyAudioRequestGetter;
import com.priamoryki.discordbot.api.audio.customsources.ProxyAudioSourceManager;
import com.priamoryki.discordbot.common.auth.holder.TokenHolder;
import com.priamoryki.discordbot.common.auth.service.AuthService;
import com.priamoryki.discordbot.common.auth.service.AuthTokenService;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pavel Lymar
 */
public class SpotifyAudioSourceManager extends ProxyAudioSourceManager {
    private static final Pattern TRACK_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?open\\.spotify\\.com/track/(?<track>[^&=%?]{22})$");
    private static final Pattern ALBUM_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?open\\.spotify\\.com/album/(?<album>[^&=%?]{22})$");
    private static final Pattern PLAYLIST_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?open\\.spotify\\.com/playlist/(?<playlist>[^&=%?]{22})$");
    private static final String SPOTIFY_CLIENT_ID_ENV_NAME = "SPOTIFY_CLIENT_ID";
    private static final String SPOTIFY_CLIENT_SECRET_ENV_NAME = "SPOTIFY_CLIENT_SECRET";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final SpotifyApi spotifyApi;
    private final AuthTokenService authTokenService;

    public SpotifyAudioSourceManager(AudioSourceManager originalAudioSourceManager) {
        super(originalAudioSourceManager);
        patterns.put(TRACK_PATTERN, new SpotifySong());
        patterns.put(ALBUM_PATTERN, new SpotifyAlbum());
        patterns.put(PLAYLIST_PATTERN, new SpotifyPlaylist());
        this.spotifyApi = SpotifyApi.builder()
                .setClientId(getSpotifyClientId())
                .setClientSecret(getSpotifyClientSecret())
                .build();
        this.authTokenService = new AuthTokenService(new SpotifyAuthService(spotifyApi), 180);
    }

    @Override
    public String getSourceName() {
        return "spotify";
    }

    private String getSpotifyClientId() {
        return System.getenv(SPOTIFY_CLIENT_ID_ENV_NAME);
    }

    private String getSpotifyClientSecret() {
        return System.getenv(SPOTIFY_CLIENT_SECRET_ENV_NAME);
    }

    private SpotifyApi getSpotifyApi() {
        spotifyApi.setAccessToken(authTokenService.getToken());
        return spotifyApi;
    }

    private String getSearchString(String author, String name) {
        return "scsearch:" + author + " - " + name;
    }

    private static class SpotifyAuthService implements AuthService {
        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final SpotifyApi spotifyApi;

        public SpotifyAuthService(SpotifyApi spotifyApi) {
            this.spotifyApi = spotifyApi;
        }

        @Override
        public TokenHolder auth() {
            try {
                ClientCredentials clientCredentials = spotifyApi.clientCredentials().build().execute();
                return new TokenHolder(
                        clientCredentials.getAccessToken(),
                        Instant.now().plusSeconds(clientCredentials.getExpiresIn()),
                        "",
                        Instant.EPOCH
                );
            } catch (IOException | ParseException | SpotifyWebApiException e) {
                logger.error("SpotifyApi login error", e);
            }
            return TokenHolder.EMPTY;
        }

        @Override
        public TokenHolder refresh(String refreshToken) {
            throw new UnsupportedOperationException();
        }
    }

    private class SpotifySong implements ProxyAudioRequestGetter {
        @Override
        public List<String> getRequests(Matcher matcher) {
            String id = matcher.group("track");
            try {
                Track track = getSpotifyApi().getTrack(id).build().execute();
                return List.of(
                        getSearchString(track.getArtists()[0].getName(), track.getName())
                );
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                logger.error("SpotifySong error", e);
            }
            return List.of();
        }
    }

    private class SpotifyAlbum implements ProxyAudioRequestGetter {
        @Override
        public List<String> getRequests(Matcher matcher) {
            String id = matcher.group("album");
            try {
                Album album = getSpotifyApi().getAlbum(id).build().execute();
                return Arrays.stream(album.getTracks().getItems()).map(
                        track -> getSearchString(track.getArtists()[0].getName(), track.getName())
                ).toList();
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                logger.error("SpotifyAlbum error", e);
            }
            return List.of();
        }
    }

    private class SpotifyPlaylist implements ProxyAudioRequestGetter {
        @Override
        public List<String> getRequests(Matcher matcher) {
            String id = matcher.group("playlist");
            try {
                Playlist playlist = getSpotifyApi().getPlaylist(id).build().execute();
                return Arrays.stream(playlist.getTracks().getItems()).map(PlaylistTrack::getTrack).map(
                        track -> getSearchString(((Track) track).getArtists()[0].getName(), track.getName())
                ).toList();
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                logger.error("SpotifyPlaylist error", e);
            }
            return List.of();
        }
    }
}
