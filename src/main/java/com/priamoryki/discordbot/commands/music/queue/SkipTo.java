package com.priamoryki.discordbot.commands.music.queue;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

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
    public String getDescription() {
        return "Skips to n's composition in queue";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "track_number", "number of the track in the queue", true)
        );
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) {
        if (args.size() == 1) {
            musicManager.getGuildMusicManager(guild).skipTo(Integer.parseInt(args.get(0)));
        }
    }
}
