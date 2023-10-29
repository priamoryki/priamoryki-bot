package com.priamoryki.discordbot.commands.playlist;

import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.entities.Playlist;
import com.priamoryki.discordbot.utils.GuildAttributesService;
import com.priamoryki.discordbot.utils.user.playlist.UserPlaylistEditor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pavel Lymar
 */
public class GetPlaylists extends PlaylistCommand {
    private final GuildAttributesService guildAttributesService;

    public GetPlaylists(UserPlaylistEditor userPlaylistEditor, GuildAttributesService guildAttributesService) {
        super(userPlaylistEditor);
        this.guildAttributesService = guildAttributesService;
    }

    @Override
    public List<String> getNames() {
        return List.of("get_playlists");
    }

    @Override
    public String getDescription() {
        return "Prints all your playlists";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of();
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) throws CommandException {
        List<Playlist> playlists = userPlaylistEditor.getUserPlaylists(member.getUser());
        String text = String.format("__%s playlists__:%n", member.getUser().getName()) +
                playlists.stream().map(
                        p -> String.format("%s (id=%d)", p.getName(), p.getId())
                ).collect(Collectors.joining("\n"));
        guildAttributesService.getOrCreateMainChannel(guild).sendMessage(text).queue();
    }
}
