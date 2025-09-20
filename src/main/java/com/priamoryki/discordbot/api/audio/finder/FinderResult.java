package com.priamoryki.discordbot.api.audio.finder;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public record FinderResult(List<AudioTrack> loadedTracks, List<Exception> exceptions) {
}
