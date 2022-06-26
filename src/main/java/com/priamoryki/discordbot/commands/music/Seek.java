package com.priamoryki.discordbot.commands.music;

import com.priamoryki.discordbot.audio.MusicManager;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pavel Lymar
 */
public class Seek extends MusicCommand {
    public Seek(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("seek");
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Message message, List<String> args) {
        if (args.size() == 1) {
            List<String> list = Arrays.stream(args.get(0).split(":")).collect(Collectors.toList());
            Collections.reverse(list);
            long time = 0;
            for (int i = 0; i < Math.min(3, list.size()); i++) {
                time += Math.pow(60, i) * Long.parseLong(list.get(i));
            }
            musicManager.getGuildMusicManager(message.getGuild()).seek(1_000 * time);
        }
    }
}
