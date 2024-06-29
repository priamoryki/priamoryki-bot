package com.priamoryki.discordbot.configs;

import com.priamoryki.discordbot.api.audio.customsources.spotify.SpotifyAudioSourceManager;
import com.priamoryki.discordbot.api.audio.customsources.tiktok.TikTokAudioSourceManager;
import com.priamoryki.discordbot.api.audio.customsources.yandexmusic.YandexMusicAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Pavel Lymar
 */
@Configuration
public class MusicConfig {
    @Bean
    public AudioPlayerManager getAudioPlayerManager() {
        AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
        audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
        audioPlayerManager.registerSourceManager(new YoutubeAudioSourceManager());
        audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        audioPlayerManager.registerSourceManager(new TikTokAudioSourceManager());
        audioPlayerManager.registerSourceManager(new SpotifyAudioSourceManager(SoundCloudAudioSourceManager.createDefault()));
        audioPlayerManager.registerSourceManager(new YandexMusicAudioSourceManager(SoundCloudAudioSourceManager.createDefault()));
        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
        return audioPlayerManager;
    }
}
