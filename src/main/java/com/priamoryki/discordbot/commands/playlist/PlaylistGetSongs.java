package com.priamoryki.discordbot.commands.playlist;

import com.priamoryki.discordbot.api.database.entities.Playlist;
import com.priamoryki.discordbot.api.playlist.PlaylistMessagesService;
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
public class PlaylistGetSongs extends PlaylistCommand {
    private final PlaylistMessagesService playlistMessagesService;

    public PlaylistGetSongs(UserPlaylistEditor userPlaylistEditor, PlaylistMessagesService playlistMessagesService) {
        super(userPlaylistEditor);
        this.playlistMessagesService = playlistMessagesService;
    }

    @Override
    public List<String> getNames() {
        return List.of("playlist_get_songs");
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
        playlistMessagesService.create(member, playlist);
    }
}
