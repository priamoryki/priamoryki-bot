package com.priamoryki.discordbot.api.audio.finder;

import com.priamoryki.discordbot.api.audio.SongRequest;
import com.priamoryki.discordbot.utils.Utils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author Pavel Lymar
 */
public class MusicFinder {
    private final AudioPlayerManager audioPlayerManager;
    private final List<CustomAudioSource> sources;

    public MusicFinder(AudioPlayerManager audioPlayerManager, CustomAudioSource... sources) {
        this.audioPlayerManager = audioPlayerManager;
        this.sources = List.of(sources);
    }

    public List<AudioTrack> find(SongRequest songRequest) {
        Guild guild = songRequest.getGuild();
        Member member = songRequest.getMember();
        String urlOrName = songRequest.getUrlOrName();
        List<AudioTrack> result = new ArrayList<>();

        try {
            audioPlayerManager.loadItemOrdered(this, urlOrName, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    track.setUserData(member.getUser());
                    result.add(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    if (Utils.isUrl(urlOrName)) {
                        playlist.getTracks().forEach(this::trackLoaded);
                    } else {
                        trackLoaded(playlist.getTracks().get(0));
                    }
                }

                @Override
                public void noMatches() {
                    tryCustomSources(songRequest).forEach(this::trackLoaded);
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    tryCustomSources(songRequest).forEach(this::trackLoaded);
                }
            }).get();
        } catch (ExecutionException | InterruptedException e) {
            System.err.println(e.getMessage());
        }
        return result;
    }

    private List<AudioTrack> tryCustomSources(SongRequest songRequest) {
        for (CustomAudioSource source : sources) {
            if (source.matches(songRequest.getUrlOrName())) {
                source.find(songRequest).forEach(this::find);
            }
        }
        return new ArrayList<>();
    }
}
