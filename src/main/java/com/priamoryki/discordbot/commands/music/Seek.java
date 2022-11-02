package com.priamoryki.discordbot.commands.music;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

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
    public String getDescription() {
        return "Seeks current track to the entered time";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "time", "time to skip current track to", true)
        );
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) {
        if (args.size() == 1) {
            List<String> list = Arrays.stream(args.get(0).split(":")).collect(Collectors.toList());
            Collections.reverse(list);
            long time = 0;
            for (int i = 0; i < Math.min(3, list.size()); i++) {
                time += Math.pow(60, i) * Long.parseLong(list.get(i));
            }
            musicManager.getGuildMusicManager(guild).seek(1_000 * time);
        }
    }
}
