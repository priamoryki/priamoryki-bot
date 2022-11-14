package com.priamoryki.discordbot.commands.music.modifiers;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class SetSpeed extends MusicCommand {
    public SetSpeed(MusicManager musicManager) {
        super(musicManager);
    }

    public List<String> getNames() {
        return List.of("set_speed");
    }

    @Override
    public String getDescription() {
        return "Sets music speed multiplier";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "multiplier", "speed multiplier", true)
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
        double speed;
        try {
            speed = Double.parseDouble(args.get(0));
        } catch (Exception e) {
            throw new CommandException("Argument isn't double!");
        }
//        if (0 >= speed || speed > 2) {
//            throw new CommandException("Argument must be in bounds (0, 2]!");
//        }
        musicManager.getGuildMusicManager(guild).setSpeed(speed);
    }
}
