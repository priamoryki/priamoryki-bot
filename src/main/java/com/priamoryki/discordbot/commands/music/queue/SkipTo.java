package com.priamoryki.discordbot.commands.music.queue;

import com.priamoryki.discordbot.audio.MusicManager;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class SkipTo extends MusicCommand {
    public SkipTo(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("skip_to");
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Message message, List<String> args) {
        if (args.size() == 1) {
            musicManager.getGuildMusicManager(message.getGuild()).skipTo(Integer.parseInt(args.get(0)));
        }
    }
}
