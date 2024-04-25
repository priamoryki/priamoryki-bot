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
public class QueueDelete extends MusicCommand {
    public QueueDelete(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("queue_delete");
    }

    @Override
    public String getDescription() {
        return "Deletes n's track from queue";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "from_id", "first index of sublist to delete from the queue", true),
                new OptionData(OptionType.INTEGER, "to_id", "last index of sublist to delete from the queue", false)
        );
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) throws CommandException {
        if (args.isEmpty() || args.size() > 2) {
            throw new CommandException("Invalid number of arguments!");
        }
        int from = Integer.parseInt(args.get(0));
        int to = args.size() == 1 ? from : Integer.parseInt(args.get(1));
        musicManager.getGuildMusicManager(guild).deleteFromQueue(from, to);
    }
}
