package com.priamoryki.discordbot.commands.music;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.api.audio.SongRequest;
import com.priamoryki.discordbot.commands.MusicCommand;
import com.priamoryki.discordbot.utils.Utils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class Music extends MusicCommand {
    public Music(MusicManager musicManager) {
        super(musicManager);
    }

    @Override
    public List<String> getNames() {
        return List.of("music", "музыка");
    }

    @Override
    public String getDescription() {
        return "Adds music by url or aliases to the queue";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "url_or_query", "URL or query for searching track", true)
        );
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) {
        if (args.isEmpty()) {
            return;
        }
        if (args.size() == 1 && Utils.isUrl(args.get(0))) {
            musicManager.play(new SongRequest(guild, member, args.get(0)));
            return;
        }
        musicManager.play(
                new SongRequest(
                        guild,
                        member,
                        "ytsearch:" + String.join(" ", args)
                )
        );
    }
}