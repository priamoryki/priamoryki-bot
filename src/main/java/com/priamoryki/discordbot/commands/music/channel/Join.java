package com.priamoryki.discordbot.commands.music.channel;

import com.priamoryki.discordbot.audio.MusicManager;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Message;

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
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Message message, List<String> args) {
        musicManager.getGuildMusicManager(message.getGuild()).join(message.getMember());
    }
}
