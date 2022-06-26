package com.priamoryki.discordbot;

import com.priamoryki.discordbot.audio.MusicManager;
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
import com.priamoryki.discordbot.commands.music.modifiers.NightCore;
import com.priamoryki.discordbot.commands.music.modifiers.Reset;
import com.priamoryki.discordbot.commands.music.queue.*;
import com.priamoryki.discordbot.commands.sounds.*;
import com.priamoryki.discordbot.events.EventsListener;
import com.priamoryki.discordbot.utils.DataSource;

/**
 * @author Pavel Lymar
 */
public class Bot {
    public static void main(String[] args) {
        try {
            DataSource data = new DataSource();

            MusicManager musicManager = new MusicManager(data);

            CommandsStorage commands = new CommandsStorage(
                    // Chat commands
                    new Clear(data),
                    new ClearAll(data),
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
                    // Print queue commands
                    new PrintQueue(musicManager),
                    new PreviousPage(musicManager),
                    new NextPage(musicManager),
                    // Modifiers
                    new BassBoost(musicManager),
                    new NightCore(musicManager),
                    new Reset(musicManager),
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

            data.getBot().addEventListener(new EventsListener(data, commands));
        } catch (Exception e) {
            System.err.println("Error on bot start occurred: " + e.getMessage());
        }
    }
}
