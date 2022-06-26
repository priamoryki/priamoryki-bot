package com.priamoryki.discordbot.commands.sounds;

import com.priamoryki.discordbot.audio.MusicManager;
import com.priamoryki.discordbot.audio.SongRequest;
import com.priamoryki.discordbot.commands.Command;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public abstract class Sound implements Command {
    private final String filename;
    private final MusicManager musicManager;

    public Sound(MusicManager musicManager, String filename) {
        this.musicManager = musicManager;
        this.filename = filename;
    }

    @Override
    public List<String> getNames() {
        return List.of(getClass().getSimpleName().toLowerCase());
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Message message, List<String> args) {
        musicManager.play(new SongRequest(message.getGuild(), message.getMember(), filename));
    }
}
