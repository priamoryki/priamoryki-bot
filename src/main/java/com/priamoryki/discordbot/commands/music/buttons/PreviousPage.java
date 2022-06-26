package com.priamoryki.discordbot.commands.music.buttons;

import com.priamoryki.discordbot.audio.MusicManager;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class PreviousPage extends MusicCommand {
    public PreviousPage(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("previous_page");
    }

    @Override
    public boolean isAvailableFromChat() {
        return false;
    }

    @Override
    public void execute(Message message, List<String> args) {
        musicManager.getGuildMusicManager(message.getGuild()).getQueueMessage().getPreviousPage();
    }
}
