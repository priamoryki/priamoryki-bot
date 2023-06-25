package com.priamoryki.discordbot.commands.playlist;

import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.utils.user.playlist.UserPlaylistEditor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class CreatePlaylist extends PlaylistCommand {
    public CreatePlaylist(UserPlaylistEditor userPlaylistEditor) {
        super(userPlaylistEditor);
    }

    @Override
    public List<String> getNames() {
        return List.of("create_playlist");
    }

    @Override
    public String getDescription() {
        return "Creates new playlist with given name";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.STRING, "name", "playlist name", true)
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
        userPlaylistEditor.createPlaylist(member.getUser(), args.get(0));
    }
}
