package com.priamoryki.discordbot.api.audio.finder;

import com.priamoryki.discordbot.api.audio.SongRequest;
import com.priamoryki.discordbot.common.Utils;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Pavel Lymar
 */
@Service
public class MusicFinder {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AudioPlayerManager audioPlayerManager;

    public MusicFinder(AudioPlayerManager audioPlayerManager) {
        this.audioPlayerManager = audioPlayerManager;
    }

    public List<AudioTrack> find(SongRequest songRequest) {
        List<AudioTrack> result = new ArrayList<>();
        Guild guild = songRequest.getGuild();
        Member member = songRequest.getMember();
        String urlOrName = songRequest.getUrlOrName();
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
                logger.info("No matches for request {}", urlOrName);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                logger.info("Load failed for request {}", urlOrName, e);
            }
        });
        return result.stream().filter(Objects::nonNull).toList();
    }
}
