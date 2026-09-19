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
public class PlaylistEdit extends PlaylistCommand {
    public PlaylistEdit(UserPlaylistEditor userPlaylistEditor) {
        super(userPlaylistEditor);
    }

    @Override
    public List<String> getNames() {
        return List.of("playlist_edit");
    }

    @Override
    public String getDescription() {
        return "Sets playlist to edit";
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
        userPlaylistEditor.changePlaylistToEdit(member.getUser(), Long.parseLong(args.get(0)));
    }
}
