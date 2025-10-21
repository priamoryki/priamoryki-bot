package com.priamoryki.discordbot.commands.music.channel;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class Join extends MusicCommand {
    public Join(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("join");
    }

    @Override
    public String getDescription() {
        return "Adds bot to your channel";
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) throws CommandException {
        if (musicManager.getGuildMusicManager(guild).getMusicData().isPlaying()) {
            throw new CommandException("Bot is playing audio now!");
        }
        musicManager.getGuildMusicManager(guild).join(member);
    }

    @Override
    public void executeWithPermissions(Guild guild, Member member, List<String> args) throws CommandException {
        execute(guild, member, args);
    }
}
