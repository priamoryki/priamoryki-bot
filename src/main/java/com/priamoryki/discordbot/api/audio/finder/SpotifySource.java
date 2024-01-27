package com.priamoryki.discordbot.api.audio.finder;

import com.priamoryki.discordbot.api.audio.SongRequest;
import com.priamoryki.discordbot.utils.auth.holder.TokenHolder;
import com.priamoryki.discordbot.utils.auth.service.AuthService;
import com.priamoryki.discordbot.utils.auth.service.AuthTokenService;
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
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pavel Lymar
 */
public class SpotifySource extends CustomAudioSource {
    private static final Pattern TRACK_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?open\\.spotify\\.com/track/([^&=%\\?]{22})$");
    private static final Pattern ALBUM_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?open\\.spotify\\.com/album/([^&=%\\?]{22})$");
    private static final Pattern PLAYLIST_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?open\\.spotify\\.com/playlist/([^&=%\\?]{22})$");
    private static final String SPOTIFY_CLIENT_ID_ENV_NAME = "SPOTIFY_CLIENT_ID";
    private static final String SPOTIFY_CLIENT_SECRET_ENV_NAME = "SPOTIFY_CLIENT_SECRET";
    private final Logger logger = LoggerFactory.getLogger(SpotifySource.class);
    private final SpotifyApi spotifyApi;
    private final AuthTokenService authTokenService;

    public SpotifySource() {
        super();
        patterns.put(TRACK_PATTERN, new SpotifySong());
        patterns.put(ALBUM_PATTERN, new SpotifyAlbum());
        patterns.put(PLAYLIST_PATTERN, new SpotifyPlaylist());
        this.spotifyApi = SpotifyApi.builder()
                .setClientId(getSpotifyClientId())
                .setClientSecret(getSpotifyClientSecret())
                .build();
        this.authTokenService = new AuthTokenService(new SpotifyAuthService(spotifyApi), 180);
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
        private final Logger logger = LoggerFactory.getLogger(SpotifyAuthService.class);
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

    private class SpotifySong implements Function<SongRequest, List<SongRequest>> {
        @Override
        public List<SongRequest> apply(SongRequest songRequest) {
            Matcher matcher = TRACK_PATTERN.matcher(songRequest.getUrlOrName());
            matcher.find();
            String id = matcher.group(3);
            try {
                Track track = getSpotifyApi().getTrack(id).build().execute();
                return List.of(
                        new SongRequest(
                                songRequest.getGuild(),
                                songRequest.getMember(),
                                getSearchString(track.getArtists()[0].getName(), track.getName())
                        )
                );
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                logger.error("SpotifySong error", e);
            }
            return List.of();
        }
    }

    private class SpotifyAlbum implements Function<SongRequest, List<SongRequest>> {
        @Override
        public List<SongRequest> apply(SongRequest songRequest) {
            Matcher matcher = ALBUM_PATTERN.matcher(songRequest.getUrlOrName());
            matcher.find();
            String id = matcher.group(3);
            try {
                Album album = getSpotifyApi().getAlbum(id).build().execute();
                return Arrays.stream(album.getTracks().getItems()).map(track -> new SongRequest(
                        songRequest.getGuild(),
                        songRequest.getMember(),
                        getSearchString(track.getArtists()[0].getName(), track.getName())
                )).toList();
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                logger.error("SpotifyAlbum error", e);
            }
            return List.of();
        }
    }

    private class SpotifyPlaylist implements Function<SongRequest, List<SongRequest>> {
        @Override
        public List<SongRequest> apply(SongRequest songRequest) {
            Matcher matcher = ALBUM_PATTERN.matcher(songRequest.getUrlOrName());
            matcher.find();
            String id = matcher.group(3);
            try {
                Playlist playlist = getSpotifyApi().getPlaylist(id).build().execute();
                return Arrays.stream(playlist.getTracks().getItems()).map(PlaylistTrack::getTrack).map(track -> new SongRequest(
                        songRequest.getGuild(),
                        songRequest.getMember(),
                        getSearchString(((Track) track).getArtists()[0].getName(), track.getName())
                )).toList();
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                logger.error("SpotifyPlaylist error", e);
            }
            return List.of();
        }
    }
}
