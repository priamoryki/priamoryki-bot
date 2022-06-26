package com.priamoryki.discordbot.commands.music;

import com.priamoryki.discordbot.audio.MusicManager;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class Repeat extends MusicCommand {
    public Repeat(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("repeat");
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Message message, List<String> args) {
        if (args.isEmpty()) {
            musicManager.getGuildMusicManager(message.getGuild()).reverseRepeat();
        } else if (args.size() == 1) {
            if (args.get(0).equalsIgnoreCase("YES")) {
                musicManager.getGuildMusicManager(message.getGuild()).setRepeat(true);
            } else if (args.get(0).equalsIgnoreCase("NO")) {
                musicManager.getGuildMusicManager(message.getGuild()).setRepeat(false);
            }
        }
    }
}
