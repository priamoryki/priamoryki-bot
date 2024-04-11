package com.priamoryki.discordbot.api.audio.customsources.yandexmusic;

import com.priamoryki.discordbot.api.audio.customsources.ProxyAudioRequestGetter;
import com.priamoryki.discordbot.api.audio.customsources.ProxyAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pavel Lymar
 */
public class YandexMusicAudioSourceManager extends ProxyAudioSourceManager {
    private static final Pattern TRACK_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?music\\.yandex\\.[a-z]+/album/(\\d+)/track/(?<track>\\d+)$");
    private static final Pattern ALBUM_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?music\\.yandex\\.[a-z]+/album/(?<album>\\d+)$");
    private static final Pattern PLAYLIST_PATTERN =
            Pattern.compile("^(https?://)?(www\\.)?music\\.yandex\\.[a-z]+/users/(?<user>.+)/playlists/(?<playlist>\\d+)$");
    private static final String API_HOST = "https://api.music.yandex.net";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public YandexMusicAudioSourceManager(AudioSourceManager originalAudioSourceManager) {
        super(originalAudioSourceManager);
        patterns.put(TRACK_PATTERN, new YandexMusicSong());
        patterns.put(ALBUM_PATTERN, new YandexMusicAlbum());
        patterns.put(PLAYLIST_PATTERN, new YandexMusicPlaylist());
    }

    @Override
    public String getSourceName() {
        return "yandex-music";
    }

    private List<String> getSongsRequests(JSONArray tracks) throws JSONException {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < tracks.length(); i++) {
            JSONObject track = tracks.getJSONObject(i);
            String author = track.getJSONArray("artists").getJSONObject(0).getString("name");
            String name = track.getString("title");
            result.add(getSearchString(author, name));
        }
        return result;
    }

    private String getSearchString(String author, String name) {
        return "scsearch:" + author + " - " + name;
    }

    private class YandexMusicSong implements ProxyAudioRequestGetter {
        @Override
        public List<String> getRequests(Matcher matcher) {
            String id = matcher.group("track");
            String url = API_HOST + "/tracks/" + id;
            try {
                String body = Jsoup.connect(url).ignoreContentType(true).execute().body();
                JSONArray json = new JSONObject(body).getJSONArray("result");
                return getSongsRequests(json);
            } catch (JSONException | IOException e) {
                logger.error("YandexMusicSong error", e);
            }
            return List.of();
        }
    }

    private class YandexMusicAlbum implements ProxyAudioRequestGetter {
        @Override
        public List<String> getRequests(Matcher matcher) {
            String id = matcher.group("album");
            String url = API_HOST + "/albums/" + id + "/with-tracks";
            try {
                String body = Jsoup.connect(url).ignoreContentType(true).execute().body();
                JSONArray json = new JSONObject(body).getJSONObject("result").getJSONArray("volumes").getJSONArray(0);
                return getSongsRequests(json);
            } catch (JSONException | IOException e) {
                logger.error("YandexMusicAlbum error", e);
            }
            return List.of();
        }
    }

    private class YandexMusicPlaylist implements ProxyAudioRequestGetter {
        @Override
        public List<String> getRequests(Matcher matcher) {
            String user = matcher.group("user");
            String id = matcher.group("playlist");
            String url = API_HOST + "/users/" + user + "/playlists/" + id;
            try {
                String body = Jsoup.connect(url).ignoreContentType(true).execute().body();
                JSONArray tracks = new JSONObject(body).getJSONObject("result").getJSONArray("tracks");
                JSONArray json = new JSONArray();
                for (int i = 0; i < tracks.length(); i++) {
                    json.put(tracks.getJSONObject(i).getJSONObject("track"));
                }
                return getSongsRequests(json);
            } catch (JSONException | IOException e) {
                logger.error("YandexMusicPlaylist error", e);
            }
            return List.of();
        }
    }
}
