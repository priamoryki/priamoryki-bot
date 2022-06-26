package com.priamoryki.discordbot.commands.sounds;

import com.priamoryki.discordbot.audio.MusicManager;

/**
 * @author Pavel Lymar
 */
public class Titan extends Sound {
    public Titan(MusicManager musicManager) {
        super(musicManager, "sounds/titan.mp3");
    }
}
