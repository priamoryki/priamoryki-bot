package com.priamoryki.discordbot.commands.playlist;

import com.priamoryki.discordbot.api.playlist.UserPlaylistEditor;
import com.priamoryki.discordbot.commands.CommandException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class PlaylistDeleteSongs extends PlaylistCommand {
    public PlaylistDeleteSongs(UserPlaylistEditor userPlaylistEditor) {
        super(userPlaylistEditor);
    }

    @Override
    public List<String> getNames() {
        return List.of("playlist_delete_songs");
    }

    @Override
    public String getDescription() {
        return "Removes songs from playlist by indexes";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "from_id", "first index of sublist to delete from the playlist", true),
                new OptionData(OptionType.INTEGER, "to_id", "last index of sublist to delete from the queue", true)
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
        userPlaylistEditor.removeSongs(member.getUser(), from, to);
    }
}
