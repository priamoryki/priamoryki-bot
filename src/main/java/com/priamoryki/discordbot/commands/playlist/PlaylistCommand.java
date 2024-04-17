package com.priamoryki.discordbot.commands.playlist;

import com.priamoryki.discordbot.api.playlist.UserPlaylistEditor;
import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.commands.CommandException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public abstract class PlaylistCommand implements Command {
    protected final UserPlaylistEditor userPlaylistEditor;

    protected PlaylistCommand(UserPlaylistEditor userPlaylistEditor) {
        this.userPlaylistEditor = userPlaylistEditor;
    }

    @Override
    public void executeWithPermissions(Guild guild, Member member, List<String> args) throws CommandException {
        execute(guild, member, args);
    }
}
