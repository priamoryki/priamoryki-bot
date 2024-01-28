package com.priamoryki.discordbot.api.audio.finder;

import com.priamoryki.discordbot.api.audio.SongRequest;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pavel Lymar
 */
public class YandexMusicSource extends CustomAudioSource {
    private static final Pattern TRACK_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?music\\.yandex\\.[a-z]+/album/(\\d+)/track/(\\d+)$");
    private static final Pattern ALBUM_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?music\\.yandex\\.[a-z]+/album/(\\d+)$");
    private static final Pattern PLAYLIST_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?music\\.yandex\\.[a-z]+/users/(.+)/playlists/(\\d+)$");
    private static final String API_HOST = "https://api.music.yandex.net";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public YandexMusicSource() {
        super();
        patterns.put(TRACK_PATTERN, new YandexMusicSong());
        patterns.put(ALBUM_PATTERN, new YandexMusicAlbum());
        patterns.put(PLAYLIST_PATTERN, new YandexMusicPlaylist());
    }

    private List<SongRequest> getSongsRequests(Guild guild, Member member, JSONArray tracks) throws JSONException {
        List<SongRequest> result = new ArrayList<>();
        for (int i = 0; i < tracks.length(); i++) {
            JSONObject track = tracks.getJSONObject(i);
            String author = track.getJSONArray("artists").getJSONObject(0).getString("name");
            String name = track.getString("title");
            result.add(new SongRequest(
                    guild,
                    member,
                    getSearchString(author, name)
            ));
        }
        return result;
    }

    private String getSearchString(String author, String name) {
        return "scsearch:" + author + " - " + name;
    }

    private class YandexMusicSong implements Function<SongRequest, List<SongRequest>> {
        @Override
        public List<SongRequest> apply(SongRequest songRequest) {
            Matcher matcher = TRACK_PATTERN.matcher(songRequest.getUrlOrName());
            matcher.find();
            String id = matcher.group(4);
            String url = API_HOST + "/tracks/" + id;
            try {
                String body = Jsoup.connect(url).ignoreContentType(true).execute().body();
                JSONArray json = new JSONObject(body).getJSONArray("result");
                return getSongsRequests(songRequest.getGuild(), songRequest.getMember(), json);
            } catch (JSONException | IOException e) {
                logger.error("YandexMusicSong error", e);
            }
            return List.of();
        }
    }

    private class YandexMusicAlbum implements Function<SongRequest, List<SongRequest>> {
        @Override
        public List<SongRequest> apply(SongRequest songRequest) {
            Matcher matcher = ALBUM_PATTERN.matcher(songRequest.getUrlOrName());
            matcher.find();
            String id = matcher.group(3);
            String url = API_HOST + "/albums/" + id + "/with-tracks";
            try {
                String body = Jsoup.connect(url).ignoreContentType(true).execute().body();
                JSONArray json = new JSONObject(body).getJSONObject("result").getJSONArray("volumes").getJSONArray(0);
                return getSongsRequests(songRequest.getGuild(), songRequest.getMember(), json);
            } catch (JSONException | IOException e) {
                logger.error("YandexMusicAlbum error", e);
            }
            return List.of();
        }
    }

    private class YandexMusicPlaylist implements Function<SongRequest, List<SongRequest>> {
        @Override
        public List<SongRequest> apply(SongRequest songRequest) {
            Matcher matcher = PLAYLIST_PATTERN.matcher(songRequest.getUrlOrName());
            matcher.find();
            String user = matcher.group(3);
            String id = matcher.group(4);
            String url = API_HOST + "/users/" + user + "/playlists/" + id;
            try {
                String body = Jsoup.connect(url).ignoreContentType(true).execute().body();
                JSONArray tracks = new JSONObject(body).getJSONObject("result").getJSONArray("tracks");
                JSONArray json = new JSONArray();
                for (int i = 0; i < tracks.length(); i++) {
                    json.put(tracks.getJSONObject(i).getJSONObject("track"));
                }
                return getSongsRequests(songRequest.getGuild(), songRequest.getMember(), json);
            } catch (JSONException | IOException e) {
                logger.error("YandexMusicPlaylist error", e);
            }
            return List.of();
        }
    }
}
