package com.priamoryki.discordbot.commands.music.buttons;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

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
    public void execute(Guild guild, Member member, List<String> args) {
        musicManager.getGuildMusicManager(guild).getQueueMessage().previousPage();
    }
}
