package com.priamoryki.discordbot.commands.music.modifiers;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

import static com.priamoryki.discordbot.common.Utils.parseTime;

/**
 * @author Pavel Lymar
 */
public class Cycle extends MusicCommand {
    public Cycle(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("cycle");
    }

    @Override
    public String getDescription() {
        return "Cycles current track";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "start_time", "time of cycle start", true),
                new OptionData(OptionType.STRING, "end_time", "time of cycle end", true)
        );
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) throws CommandException {
        if (args.size() != 2) {
            throw new CommandException("Invalid number of arguments!");
        }
        long start = parseTime(args.get(0));
        long finish = parseTime(args.get(1));
        musicManager.getGuildMusicManager(guild).cycle(1_000L * start, 1_000L * finish);
    }
}
