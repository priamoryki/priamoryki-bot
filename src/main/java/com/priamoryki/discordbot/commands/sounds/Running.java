package com.priamoryki.discordbot.commands.sounds;

import com.priamoryki.discordbot.api.audio.MusicManager;

/**
 * @author Pavel Lymar
 */
public class Running extends Sound {
    public Running(MusicManager musicManager) {
        super(musicManager, "sounds/running.mp3");
    }
}
