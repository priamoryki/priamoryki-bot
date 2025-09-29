package com.priamoryki.discordbot.commands.music;

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
public class Repeat extends MusicCommand {
    public Repeat(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("repeat");
    }

    @Override
    public String getDescription() {
        return "Sets on_repeat modifier";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "toggle", "ON or OFF option")
        );
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) throws CommandException {
        if (args.isEmpty()) {
            musicManager.getGuildMusicManager(guild).reverseRepeat();
            return;
        }
        if (args.size() != 1) {
            throw new CommandException("Invalid number of arguments!");
        }
        if (args.getFirst().equalsIgnoreCase("ON")) {
            musicManager.getGuildMusicManager(guild).setRepeat(true);
        } else if (args.getFirst().equalsIgnoreCase("OFF")) {
            musicManager.getGuildMusicManager(guild).setRepeat(false);
        } else {
            throw new CommandException("Unknown argument! Only ON | OFF accepted!");
        }
    }
}
