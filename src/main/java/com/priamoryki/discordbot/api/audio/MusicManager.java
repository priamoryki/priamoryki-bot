package com.priamoryki.discordbot.api.audio;

import com.priamoryki.discordbot.api.audio.finder.MusicFinder;
import com.priamoryki.discordbot.api.common.ExceptionNotifier;
import com.priamoryki.discordbot.api.common.GuildAttributesService;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Lymar, Michael Ruzavin
 */
@Service
public class MusicManager {
    private final GuildAttributesService guildAttributesService;
    private final AudioPlayerManager audioPlayerManager;
    private final MusicFinder musicFinder;
    private final ExceptionNotifier exceptionNotifier;
    private final ObjectProvider<GuildMusicManager> guildMusicManagerProvider;
    private final Map<Long, GuildMusicManager> managers;

    public MusicManager(
            GuildAttributesService guildAttributesService,
            AudioPlayerManager audioPlayerManager,
            MusicFinder musicFinder,
            ExceptionNotifier exceptionNotifier,
            ObjectProvider<GuildMusicManager> guildMusicManagerProvider
    ) {
        this.guildAttributesService = guildAttributesService;
        this.audioPlayerManager = audioPlayerManager;
        this.musicFinder = musicFinder;
        this.exceptionNotifier = exceptionNotifier;
        this.guildMusicManagerProvider = guildMusicManagerProvider;
        this.managers = new HashMap<>();
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return managers.computeIfAbsent(
                guild.getIdLong(),
                guildId -> {
                    GuildMusicManager guildMusicManager = guildMusicManagerProvider.getObject(
                            guildAttributesService, audioPlayerManager, musicFinder, exceptionNotifier, guild
                    );
                    guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
                    return guildMusicManager;
                }
        );
    }
}
