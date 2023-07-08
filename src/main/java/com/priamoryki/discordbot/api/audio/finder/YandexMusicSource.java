package com.priamoryki.discordbot.api.audio.finder;

import com.priamoryki.discordbot.api.audio.SongRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Pavel Lymar
 */
public class YandexMusicSource extends CustomAudioSource {
    private static final String ARTIST = "d-artists";
    private static final String PLAYLIST_ARTISTS = "d-track__artists";
    private static final String TRACK_TITLE = "sidebar-track__title";
    private static final String TRACKS_TITLES = "d-track__name";

    public YandexMusicSource() {
        super();
        patterns.put(
                Pattern.compile("^(https?://)?(www\\.)?music\\.yandex\\.[a-z]+/album/[0-9]+/track/[0-9]+$"),
                new YandexMusicSong()
        );
        patterns.put(
                Pattern.compile("^(https?://)?(www\\.)?music\\.yandex\\.[a-z]+/album/[0-9]+$"),
                new YandexMusicAlbum()
        );
        patterns.put(
                Pattern.compile("^(https?://)?(www\\.)?music\\.yandex\\.[a-z]+/users/.+/playlists/[0-9]+$"),
                new YandexMusicPlaylist()
        );
    }

    private class YandexMusicSong implements Function<SongRequest, List<SongRequest>> {
        @Override
        public List<SongRequest> apply(SongRequest songRequest) {
            return fromYandexMusic(songRequest, TRACK_TITLE);
        }
    }

    private class YandexMusicAlbum implements Function<SongRequest, List<SongRequest>> {
        @Override
        public List<SongRequest> apply(SongRequest songRequest) {
            return fromYandexMusic(songRequest, TRACKS_TITLES);
        }
    }

    private class YandexMusicPlaylist implements Function<SongRequest, List<SongRequest>> {
        @Override
        public List<SongRequest> apply(SongRequest songRequest) {
            return fromYandexMusic(songRequest, TRACKS_TITLES);
        }
    }

    private List<SongRequest> fromYandexMusic(SongRequest songRequest, String className) {
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
            return IntStream.range(0, tracks.size()).mapToObj(
                    i -> {
                        String artist = artists.get(i);
                        artist = !artist.isEmpty() ? artist : doc.getElementsByClass(ARTIST).get(0).text();
                        return new SongRequest(
                                songRequest.getGuild(),
                                songRequest.getMember(),
                                "scsearch:" + artist + " - " + tracks.get(i)
                        );
                    }
            ).collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("YandexMusic error: " + e.getMessage());
        }
        return new ArrayList<>();
    }
}
