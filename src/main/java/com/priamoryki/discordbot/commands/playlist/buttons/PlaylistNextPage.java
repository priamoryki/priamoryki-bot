package com.priamoryki.discordbot.commands.playlist.buttons;

import com.priamoryki.discordbot.api.playlist.PlaylistMessagesService;
import com.priamoryki.discordbot.api.playlist.UserPlaylistEditor;
import com.priamoryki.discordbot.commands.playlist.PlaylistCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class PlaylistNextPage extends PlaylistCommand {
    private final PlaylistMessagesService playlistMessagesService;

    public PlaylistNextPage(UserPlaylistEditor userPlaylistEditor, PlaylistMessagesService playlistMessagesService) {
        super(userPlaylistEditor);
        this.playlistMessagesService = playlistMessagesService;
    }

    @Override
    public List<String> getNames() {
        return List.of("playlist_next_page");
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) {
        playlistMessagesService.nextPage(member);
    }
}
