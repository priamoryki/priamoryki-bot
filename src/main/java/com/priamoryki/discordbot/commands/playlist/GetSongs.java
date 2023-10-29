package com.priamoryki.discordbot.commands.playlist;

import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.entities.Playlist;
import com.priamoryki.discordbot.entities.PlaylistSong;
import com.priamoryki.discordbot.utils.DataSource;
import com.priamoryki.discordbot.utils.user.playlist.UserPlaylistEditor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Pavel Lymar
 */
public class GetSongs extends PlaylistCommand {
    private final DataSource data;

    public GetSongs(UserPlaylistEditor userPlaylistEditor, DataSource data) {
        super(userPlaylistEditor);
        this.data = data;
    }

    @Override
    public List<String> getNames() {
        return List.of("get_playlist_songs");
    }

    @Override
    public String getDescription() {
        return "Prints playlist songs";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "playlist_id", "playlist id", true)
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
        Long id = Long.parseLong(args.get(0));
        Playlist playlist = userPlaylistEditor.getPlaylist(member.getUser(), id);
        List<PlaylistSong> songs = playlist.getSongs();
        String text = String.format("__%s (id=%d) playlist songs__:%n", playlist.getName(), id) +
                IntStream.range(0, songs.size()).mapToObj(
                        i -> String.format(
                                "%d) `%s`",
                                i + 1,
                                songs.get(i).getName()
                        )
                ).collect(Collectors.joining("\n"));
        data.getOrCreateMainChannel(guild).sendMessage(text).queue();
    }
}
