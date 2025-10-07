package com.priamoryki.discordbot.commands.music;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.commands.MusicCommand;
import com.priamoryki.discordbot.common.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

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
    public void execute(Guild guild, Member member, List<String> args) throws CommandException {
        if (args.size() != 1) {
            throw new CommandException("Invalid number of arguments!");
        }
        long time = Utils.parseTime(args.getFirst());
        musicManager.getGuildMusicManager(guild).seek(1_000L * time);
    }
}
