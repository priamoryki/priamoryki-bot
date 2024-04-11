package com.priamoryki.discordbot.api.audio.customsources;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * @author Pavel Lymar
 */
public abstract class ProxyAudioSourceManager implements AudioSourceManager {
    protected final AudioSourceManager originalAudioSourceManager;
    protected final Map<Pattern, ProxyAudioRequestGetter> patterns;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutorService downloaders;

    protected ProxyAudioSourceManager(AudioSourceManager originalAudioSourceManager) {
        this.originalAudioSourceManager = originalAudioSourceManager;
        this.patterns = new HashMap<>();
        this.downloaders = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        for (var entry : patterns.entrySet()) {
            var pattern = entry.getKey();
            var function = entry.getValue();
            Matcher matcher = pattern.matcher(reference.identifier);
            if (matcher.find()) {
                var requests = function.getRequests(matcher);
                List<AudioTrack> result = new ArrayList<>(requests.size());
                for (int i = 0; i < requests.size(); i++) {
                    result.add(null);
                }
                Phaser phaser = new Phaser();
                phaser.register();
                IntStream.range(0, requests.size()).forEach(i -> {
                    String urlOrName = requests.get(i);
                    phaser.register();
                    downloaders.submit(() -> {
                        var searchResult = originalAudioSourceManager.loadItem(manager, new AudioReference(urlOrName, urlOrName));
                        if (searchResult instanceof AudioTrack audioTrack) {
                            result.set(i, audioTrack);
                        } else if (searchResult instanceof AudioPlaylist audioPlaylist && !audioPlaylist.getTracks().isEmpty()) {
                            result.set(i, audioPlaylist.getTracks().get(0));
                        } else {
                            logger.debug("Cannot find audio track by request: {}", urlOrName);
                        }
                        phaser.arriveAndDeregister();
                    });
                });
                phaser.arriveAndAwaitAdvance();
                return new BasicAudioPlaylist(null, result.stream().filter(Objects::nonNull).toList(), null, false);
            }
        }
        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return originalAudioSourceManager.isTrackEncodable(track);
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        originalAudioSourceManager.encodeTrack(track, output);
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return originalAudioSourceManager.decodeTrack(trackInfo, input);
    }

    @Override
    public void shutdown() {
        originalAudioSourceManager.shutdown();
    }
}
