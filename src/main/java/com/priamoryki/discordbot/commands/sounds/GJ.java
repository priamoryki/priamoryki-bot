package com.priamoryki.discordbot.commands.sounds;

import com.priamoryki.discordbot.api.audio.MusicManager;

/**
 * @author Pavel Lymar
 */
public class GJ extends Sound {
    public GJ(MusicManager musicManager) {
        super(musicManager, "sounds/gj.mp3");
    }
}
