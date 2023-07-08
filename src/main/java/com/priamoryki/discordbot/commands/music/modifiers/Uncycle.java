package com.priamoryki.discordbot.commands.music.modifiers;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

public class Uncycle extends MusicCommand {
    public Uncycle(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("uncycle");
    }

    @Override
    public String getDescription() {
        return "Uncycles current track";
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) throws CommandException {
        musicManager.getGuildMusicManager(guild).uncycle();
    }
}
