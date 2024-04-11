package com.priamoryki.discordbot.api.audio.customsources;

import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Pavel Lymar
 */
public interface ProxyAudioRequestGetter {
    List<String> getRequests(Matcher matcher);
}
