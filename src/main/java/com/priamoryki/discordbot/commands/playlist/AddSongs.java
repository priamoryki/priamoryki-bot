package com.priamoryki.discordbot.commands.playlist;

import com.priamoryki.discordbot.api.audio.SongRequest;
import com.priamoryki.discordbot.api.audio.finder.MusicFinder;
import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.utils.Utils;
import com.priamoryki.discordbot.utils.user.playlist.SongInfo;
import com.priamoryki.discordbot.utils.user.playlist.UserPlaylistEditor;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class AddSongs extends PlaylistCommand {
    private final MusicFinder musicFinder;

    public AddSongs(UserPlaylistEditor userPlaylistEditor, MusicFinder musicFinder) {
        super(userPlaylistEditor);
        this.musicFinder = musicFinder;
    }

    @Override
    public List<String> getNames() {
        return List.of("add_songs");
    }

    @Override
    public String getDescription() {
        return "Adds songs by url or name to playlist";
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
    public void execute(Guild guild, Member member, List<String> args) throws CommandException {
        if (args.isEmpty()) {
            throw new CommandException("Invalid number of arguments!");
        }
        String urlOrName = args.size() == 1 && Utils.isUrl(args.get(0))
                ? args.get(0)
                : "ytsearch:" + String.join(" ", args);
        List<AudioTrack> songs = musicFinder.find(new SongRequest(guild, member, urlOrName));
        List<SongInfo> songInfos = songs.stream().map(AudioTrack::getInfo)
                .map(info -> new SongInfo(info.title, info.uri, info.length))
                .toList();
        userPlaylistEditor.addSongs(
                member.getUser(),
                songInfos
        );
    }
}
