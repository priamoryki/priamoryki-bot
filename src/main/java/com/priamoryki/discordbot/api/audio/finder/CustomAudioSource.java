package com.priamoryki.discordbot.api.audio.finder;

import com.priamoryki.discordbot.api.audio.SongRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Pavel Lymar
 */
public abstract class CustomAudioSource {
    protected final Map<Pattern, Function<SongRequest, List<SongRequest>>> patterns;

    protected CustomAudioSource() {
        this.patterns = new HashMap<>();
    }

    boolean matches(String urlOrName) {
        for (Pattern pattern : patterns.keySet()) {
            if (pattern.matcher(urlOrName).matches()) {
                return true;
            }
        }
        return false;
    }

    List<SongRequest> find(SongRequest songRequest) {
        for (var entry : patterns.entrySet()) {
            var pattern = entry.getKey();
            var function = entry.getValue();
            Matcher matcher = pattern.matcher(songRequest.getUrlOrName());
            if (matcher.matches()) {
                return function.apply(songRequest);
            }
        }
        return List.of();
    }
}
