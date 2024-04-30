package com.priamoryki.discordbot.commands.music.queue;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.api.audio.SongRequest;
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
public class PlayNext extends MusicCommand {
    public PlayNext(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("play_next");
    }

    @Override
    public String getDescription() {
        return "Adds track at the beginning of the queue";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "query", "URL or query for searching track", true)
        );
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) throws CommandException {
        if (args.isEmpty()) {
            throw new CommandException("Invalid number of arguments!");
        }
        musicManager.getGuildMusicManager(guild).join(member);
        if (args.size() == 1 && Utils.isUrl(args.get(0))) {
            musicManager.getGuildMusicManager(guild).playNext(new SongRequest(guild, member, args.get(0)));
            return;
        }
        musicManager.getGuildMusicManager(guild).playNext(
                new SongRequest(
                        guild,
                        member,
                        "ytsearch:" + String.join(" ", args)
                )
        );
    }
}
