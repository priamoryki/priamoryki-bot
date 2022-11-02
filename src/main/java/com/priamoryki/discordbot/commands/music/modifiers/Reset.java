package com.priamoryki.discordbot.commands.music.modifiers;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class Reset extends MusicCommand {
    public Reset(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("reset");
    }

    @Override
    public String getDescription() {
        return "Resets all music modifiers";
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) {
        musicManager.getGuildMusicManager(guild).reset();
    }
}
