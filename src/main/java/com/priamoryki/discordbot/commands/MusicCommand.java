package com.priamoryki.discordbot.commands;

import com.priamoryki.discordbot.audio.MusicManager;

/**
 * @author Pavel Lymar
 */
public abstract class MusicCommand implements Command {
    protected final MusicManager musicManager;

    public MusicCommand(MusicManager musicManager) {
        this.musicManager = musicManager;
    }
}
