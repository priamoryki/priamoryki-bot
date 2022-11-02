package com.priamoryki.discordbot.commands.sounds;

import com.priamoryki.discordbot.api.audio.MusicManager;

/**
 * @author Pavel Lymar
 */
public class Silence extends Sound {
    public Silence(MusicManager musicManager) {
        super(musicManager, "sounds/silence.mp3");
    }
}
