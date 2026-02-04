package com.priamoryki.discordbot.configs;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.api.audio.finder.MusicFinder;
import com.priamoryki.discordbot.api.common.ExceptionNotifier;
import com.priamoryki.discordbot.api.common.GuildAttributesService;
import com.priamoryki.discordbot.api.playlist.PlaylistMessagesService;
import com.priamoryki.discordbot.api.playlist.UserPlaylistEditor;
import com.priamoryki.discordbot.commands.CommandsStorage;
import com.priamoryki.discordbot.commands.chat.ClearAll;
import com.priamoryki.discordbot.commands.music.Music;
import com.priamoryki.discordbot.commands.music.Repeat;
import com.priamoryki.discordbot.commands.music.Seek;
import com.priamoryki.discordbot.commands.music.Skip;
import com.priamoryki.discordbot.commands.music.buttons.NextPage;
import com.priamoryki.discordbot.commands.music.buttons.Pause;
import com.priamoryki.discordbot.commands.music.buttons.PreviousPage;
import com.priamoryki.discordbot.commands.music.buttons.Resume;
import com.priamoryki.discordbot.commands.music.channel.Join;
import com.priamoryki.discordbot.commands.music.channel.Leave;
import com.priamoryki.discordbot.commands.music.modifiers.BassBoost;
import com.priamoryki.discordbot.commands.music.modifiers.Cycle;
import com.priamoryki.discordbot.commands.music.modifiers.NightCore;
import com.priamoryki.discordbot.commands.music.modifiers.Reset;
import com.priamoryki.discordbot.commands.music.modifiers.Speed;
import com.priamoryki.discordbot.commands.music.modifiers.Uncycle;
import com.priamoryki.discordbot.commands.music.queue.History;
import com.priamoryki.discordbot.commands.music.queue.PlayNext;
import com.priamoryki.discordbot.commands.music.queue.QueueClear;
import com.priamoryki.discordbot.commands.music.queue.QueueDelete;
import com.priamoryki.discordbot.commands.music.queue.QueuePrint;
import com.priamoryki.discordbot.commands.music.queue.QueueShuffle;
import com.priamoryki.discordbot.commands.music.queue.SkipTo;
import com.priamoryki.discordbot.commands.playlist.PlaylistAddSongs;
import com.priamoryki.discordbot.commands.playlist.PlaylistCreate;
import com.priamoryki.discordbot.commands.playlist.PlaylistDelete;
import com.priamoryki.discordbot.commands.playlist.PlaylistDeleteSongs;
import com.priamoryki.discordbot.commands.playlist.PlaylistEdit;
import com.priamoryki.discordbot.commands.playlist.PlaylistGetAll;
import com.priamoryki.discordbot.commands.playlist.PlaylistGetSongs;
import com.priamoryki.discordbot.commands.playlist.PlaylistPlay;
import com.priamoryki.discordbot.commands.playlist.buttons.PlaylistNextPage;
import com.priamoryki.discordbot.commands.playlist.buttons.PlaylistPreviousPage;
import com.priamoryki.discordbot.commands.sounds.Boobs;
import com.priamoryki.discordbot.commands.sounds.GJ;
import com.priamoryki.discordbot.commands.sounds.Kaguya;
import com.priamoryki.discordbot.commands.sounds.Nikoni;
import com.priamoryki.discordbot.commands.sounds.Nya;
import com.priamoryki.discordbot.commands.sounds.Ohhh;
import com.priamoryki.discordbot.commands.sounds.Ohio;
import com.priamoryki.discordbot.commands.sounds.Running;
import com.priamoryki.discordbot.commands.sounds.Senpai;
import com.priamoryki.discordbot.commands.sounds.Silence;
import com.priamoryki.discordbot.commands.sounds.Titan;
import com.priamoryki.discordbot.commands.sounds.Tuturu;
import com.priamoryki.discordbot.commands.sounds.Wtf;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Pavel Lymar
 */
@Configuration
public class BotConfig {
    @Bean
    public CommandsStorage commandsStorage(
            ExceptionNotifier exceptionNotifier,
            MusicManager musicManager,
            GuildAttributesService guildAttributesService,
            UserPlaylistEditor userPlaylistEditor,
            PlaylistMessagesService playlistMessagesService,
            MusicFinder musicFinder
    ) {
        return new CommandsStorage(
                exceptionNotifier,
                // Chat commands
                new ClearAll(guildAttributesService),
                // Voice channel commands
                new Join(musicManager),
                new Leave(musicManager),
                // Play music command
                new Music(musicManager),
                // Audio commands
                new Resume(musicManager),
                new Pause(musicManager),
                new Skip(musicManager),
                new Repeat(musicManager),
                new Seek(musicManager),
                // Queue commands
                new QueueClear(musicManager),
                new QueueDelete(musicManager),
                new QueueShuffle(musicManager),
                new History(musicManager),
                new SkipTo(musicManager),
                new PlayNext(musicManager),
                // Print queue commands
                new QueuePrint(musicManager),
                new PreviousPage(musicManager),
                new NextPage(musicManager),
                // Modifiers
                new BassBoost(musicManager),
                new NightCore(musicManager),
                new Reset(musicManager),
                new Speed(musicManager),
                new Cycle(musicManager),
                new Uncycle(musicManager),
                // Sounds
                new Boobs(musicManager),
                new GJ(musicManager),
                new Kaguya(musicManager),
                new Nikoni(musicManager),
                new Nya(musicManager),
                new Ohhh(musicManager),
                new Ohio(musicManager),
                new Running(musicManager),
                new Senpai(musicManager),
                new Silence(musicManager),
                new Titan(musicManager),
                new Tuturu(musicManager),
                new Wtf(musicManager),
                // Playlist commands
                new PlaylistAddSongs(userPlaylistEditor, musicFinder, guildAttributesService),
                new PlaylistCreate(userPlaylistEditor),
                new PlaylistDelete(userPlaylistEditor),
                new PlaylistEdit(userPlaylistEditor),
                new PlaylistGetAll(userPlaylistEditor, guildAttributesService),
                new PlaylistPlay(musicManager, userPlaylistEditor),
                new PlaylistDeleteSongs(userPlaylistEditor),
                // Get playlist songs commands
                new PlaylistGetSongs(userPlaylistEditor, playlistMessagesService),
                new PlaylistPreviousPage(userPlaylistEditor, playlistMessagesService),
                new PlaylistNextPage(userPlaylistEditor, playlistMessagesService)
        );
    }

    @Bean
    public JDA jda(@Value("${TOKEN}") String token) {
        return JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
    }
}
