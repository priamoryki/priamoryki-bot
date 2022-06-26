package com.priamoryki.discordbot.commands.music;

import com.priamoryki.discordbot.audio.MusicManager;
import com.priamoryki.discordbot.audio.SongRequest;
import com.priamoryki.discordbot.commands.MusicCommand;
import com.priamoryki.discordbot.utils.Utils;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class Music extends MusicCommand {
    public Music(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("Music", "music", "Музыка", "музыка");
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Message message, List<String> args) {
        if (args.isEmpty()) {
            return;
        }
        if (args.size() == 1 && Utils.isUrl(args.get(0))) {
            musicManager.play(new SongRequest(message.getGuild(), message.getMember(), args.get(0)));
            return;
        }
        musicManager.play(
                new SongRequest(
                        message.getGuild(),
                        message.getMember(),
                        "ytsearch:" + String.join(" ", args)
                )
        );
    }
}
