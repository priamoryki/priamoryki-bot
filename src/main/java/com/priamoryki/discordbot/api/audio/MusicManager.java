package com.priamoryki.discordbot.api.audio;

import com.priamoryki.discordbot.utils.DataSource;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.entities.Guild;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Lymar
 */
public class MusicManager {
    private final DataSource data;
    private final Map<Long, GuildMusicManager> managers;
    private final AudioPlayerManager audioPlayerManager;

    public MusicManager(DataSource data) {
        this.data = data;
        this.managers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        audioPlayerManager.registerSourceManager(new LocalAudioSourceManager());
        audioPlayerManager.registerSourceManager(new YoutubeAudioSourceManager());
        audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        audioPlayerManager.getConfiguration().setFilterHotSwapEnabled(true);
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return managers.computeIfAbsent(
                guild.getIdLong(),
                guildId -> {
                    GuildMusicManager guildMusicManager = new GuildMusicManager(data, guild, audioPlayerManager);
                    guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
                    return guildMusicManager;
                }
        );
    }
}
