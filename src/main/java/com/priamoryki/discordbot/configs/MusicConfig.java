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
import dev.lavalink.youtube.YoutubeSourceOptions;
import dev.lavalink.youtube.clients.AndroidVr;
import dev.lavalink.youtube.clients.Music;
import dev.lavalink.youtube.clients.Tv;
import dev.lavalink.youtube.clients.Web;
import dev.lavalink.youtube.clients.WebEmbedded;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Pavel Lymar
 */
@Configuration
public class MusicConfig {
    @Bean
    public YoutubeAudioSourceManager youtubeAudioSourceManager(
            @Value("${YT_CIPHER_URL:}") String cipherUrl,
            @Value("${YT_CIPHER_TOKEN:}") String cipherToken
    ) {
        YoutubeSourceOptions options = new YoutubeSourceOptions();
        if (!cipherUrl.isBlank()) {
            options.setRemoteCipher(cipherUrl, cipherToken.isBlank() ? null : cipherToken, null);
        }

        return new YoutubeAudioSourceManager(options, new Music(), new Tv(), new AndroidVr(), new Web(), new WebEmbedded());
    }

    @Bean
    public AudioPlayerManager getAudioPlayerManager(YoutubeAudioSourceManager youtubeAudioSourceManager) {
        AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
        audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
        audioPlayerManager.registerSourceManager(youtubeAudioSourceManager);
        audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        audioPlayerManager.registerSourceManager(new TikTokAudioSourceManager());
        audioPlayerManager.registerSourceManager(new SpotifyAudioSourceManager(SoundCloudAudioSourceManager.createDefault()));
        audioPlayerManager.registerSourceManager(new YandexMusicAudioSourceManager(SoundCloudAudioSourceManager.createDefault()));
        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
        return audioPlayerManager;
    }
}
