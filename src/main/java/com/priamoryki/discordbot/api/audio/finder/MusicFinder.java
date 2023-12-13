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

import java.util.Arrays;
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

        Phaser phaser = new Phaser();
        AudioTrack[] result = new AudioTrack[requests.size()];
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
                        result[i] = track;
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
                        System.out.println("no matches for request " + urlOrName);
                    }

                    @Override
                    public void loadFailed(FriendlyException exception) {
                        System.out.println("load failed for request " + urlOrName);
                    }
                });
                phaser.arriveAndDeregister();
            });
        });
        phaser.arriveAndAwaitAdvance();
        return Arrays.stream(result).filter(Objects::nonNull).toList();
    }
}
