package com.priamoryki.discordbot.commands.sounds;

import com.priamoryki.discordbot.api.audio.MusicManager;

/**
 * @author Pavel Lymar
 */
public class Nya extends Sound {
    public Nya(MusicManager musicManager) {
        super(musicManager, "sounds/nya.mp3");
    }
}
