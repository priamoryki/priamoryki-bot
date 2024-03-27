package com.priamoryki.discordbot.commands;

import com.priamoryki.discordbot.api.audio.MusicManager;

/**
 * @author Pavel Lymar
 */
public abstract class MusicCommand implements Command {
    protected final MusicManager musicManager;

    protected MusicCommand(MusicManager musicManager) {
        this.musicManager = musicManager;
    }
}
