package com.priamoryki.discordbot.commands.sounds;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.api.audio.SongRequest;
import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public abstract class Sound extends MusicCommand {
    private final String filename;

    public Sound(MusicManager musicManager, String filename) {
        super(musicManager);
        this.filename = filename;
    }

    @Override
    public List<String> getNames() {
        return List.of(getClass().getSimpleName().toLowerCase());
    }

    @Override
    public String getDescription() {
        return "Adds sound to the queue";
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) throws CommandException {
        musicManager.getGuildMusicManager(guild).join(member);
        musicManager.getGuildMusicManager(guild).play(new SongRequest(guild, member, filename));
    }
}
