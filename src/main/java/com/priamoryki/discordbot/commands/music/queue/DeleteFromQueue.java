package com.priamoryki.discordbot.commands.music.queue;

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
public class DeleteFromQueue extends MusicCommand {
    public DeleteFromQueue(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("delete_from_queue");
    }

    @Override
    public String getDescription() {
        return "Deletes n's track from queue";
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
    public void execute(Guild guild, Member member, List<String> args) throws CommandException {
        if (args.size() != 1) {
            throw new CommandException("Invalid number of arguments!");
        }
        int n;
        try {
            n = Integer.parseInt(args.get(0));
        } catch (Exception e) {
            throw new CommandException("Argument isn't integer!");
        }
        if (1 > n) {
            throw new CommandException("Track number should be natural number!");
        }
        musicManager.getGuildMusicManager(guild).deleteFromQueue(n);
    }
}
