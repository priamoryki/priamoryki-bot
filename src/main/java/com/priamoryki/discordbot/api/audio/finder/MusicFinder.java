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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.stream.IntStream;

/**
 * @author Pavel Lymar
 */
public class MusicFinder {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutorService downloaders;
    private final AudioPlayerManager audioPlayerManager;
    private final List<CustomAudioSource> sources;

    public MusicFinder(AudioPlayerManager audioPlayerManager, CustomAudioSource... sources) {
        this.downloaders = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.audioPlayerManager = audioPlayerManager;
        this.sources = List.of(sources);
    }

    public List<AudioTrack> find(SongRequest songRequest) {
        List<SongRequest> requests = sources.stream()
                .filter(source -> source.matches(songRequest.getUrlOrName()))
                .findFirst().map(source -> source.find(songRequest)).orElse(List.of(songRequest));

        List<List<AudioTrack>> result = new ArrayList<>(requests.size());
        for (int i = 0; i < requests.size(); i++) {
            result.add(new ArrayList<>());
        }
        Phaser phaser = new Phaser();
        phaser.register();
        IntStream.range(0, requests.size()).forEach(i -> {
            SongRequest request = requests.get(i);
            phaser.register();
            downloaders.submit(() -> {
                Guild guild = request.getGuild();
                Member member = request.getMember();
                String urlOrName = request.getUrlOrName();
                audioPlayerManager.loadItemSync(urlOrName, new AudioLoadResultHandler() {
                    @Override
                    public void trackLoaded(AudioTrack track) {
                        track.setUserData(member.getUser());
                        result.get(i).add(track);
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
                    public void loadFailed(FriendlyException exception) {
                        logger.info("Load failed for request {}", urlOrName);
                    }
                });
                phaser.arriveAndDeregister();
            });
        });
        phaser.arriveAndAwaitAdvance();
        return result.stream().flatMap(Collection::stream).filter(Objects::nonNull).toList();
    }
}
