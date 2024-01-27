package com.priamoryki.discordbot;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.commands.CommandsStorage;
import com.priamoryki.discordbot.commands.chat.Clear;
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
import com.priamoryki.discordbot.commands.music.modifiers.SetSpeed;
import com.priamoryki.discordbot.commands.music.modifiers.Uncycle;
import com.priamoryki.discordbot.commands.music.queue.ClearQueue;
import com.priamoryki.discordbot.commands.music.queue.DeleteFromQueue;
import com.priamoryki.discordbot.commands.music.queue.PlayNext;
import com.priamoryki.discordbot.commands.music.queue.PrintQueue;
import com.priamoryki.discordbot.commands.music.queue.ShuffleQueue;
import com.priamoryki.discordbot.commands.music.queue.SkipTo;
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
import com.priamoryki.discordbot.events.EventsListener;
import com.priamoryki.discordbot.utils.BotData;
import com.priamoryki.discordbot.utils.GuildAttributesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Lymar, Michael Ruzavin
 */
@Service
public class Bot implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(Bot.class);
    private final BotData data;
    private final MusicManager musicManager;
    private final GuildAttributesService guildAttributesService;

    public Bot(BotData data, MusicManager musicManager, GuildAttributesService guildAttributesService) {
        this.data = data;
        this.musicManager = musicManager;
        this.guildAttributesService = guildAttributesService;
    }

    public void start() {
        try {
            CommandsStorage commands = new CommandsStorage(
                    // Chat commands
                    new Clear(data, guildAttributesService),
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
                    new ClearQueue(musicManager),
                    new DeleteFromQueue(musicManager),
                    new ShuffleQueue(musicManager),
                    new SkipTo(musicManager),
                    new PlayNext(musicManager),
                    // Print queue commands
                    new PrintQueue(musicManager),
                    new PreviousPage(musicManager),
                    new NextPage(musicManager),
                    // Modifiers
                    new BassBoost(musicManager),
                    new NightCore(musicManager),
                    new Reset(musicManager),
                    new SetSpeed(musicManager),
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
                    new Wtf(musicManager)
            );

            EventsListener eventsListener = new EventsListener(data, commands, guildAttributesService);
            data.setupBot(commands, eventsListener);
        } catch (Exception e) {
            logger.error("Error on bot start", e);
        }
    }

    @Override
    public void run(String... args) {
        start();
    }
}
