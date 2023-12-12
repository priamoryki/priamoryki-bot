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
        List<SongRequest> requests = sources.stream()
                .filter(source -> source.matches(songRequest.getUrlOrName()))
                .findFirst().map(source -> source.find(songRequest)).orElse(List.of(songRequest));

        List<AudioTrack> result = new ArrayList<>();
        for (SongRequest request : requests) {
            Guild guild = request.getGuild();
            Member member = request.getMember();
            String urlOrName = request.getUrlOrName();
            audioPlayerManager.loadItemSync(urlOrName, new AudioLoadResultHandler() {
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

                }

                @Override
                public void loadFailed(FriendlyException exception) {

                }
            });
        }
        return result;
    }
}
