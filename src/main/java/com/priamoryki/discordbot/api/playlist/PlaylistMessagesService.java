package com.priamoryki.discordbot.api.playlist;

import com.priamoryki.discordbot.api.common.GuildAttributesService;
import com.priamoryki.discordbot.api.database.entities.Playlist;
import com.priamoryki.discordbot.api.messages.PlaylistMessage;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Lymar
 */
@Service
public class PlaylistMessagesService {
    private final GuildAttributesService guildAttributesService;
    private final Map<Member, PlaylistMessage> playlistIdToMessage;

    public PlaylistMessagesService(GuildAttributesService guildAttributesService) {
        this.guildAttributesService = guildAttributesService;
        this.playlistIdToMessage = new HashMap<>();
    }

    public void create(Member member, Playlist playlist) {
        PlaylistMessage oldPlaylistMessage = playlistIdToMessage.get(member);
        if (oldPlaylistMessage != null) {
            oldPlaylistMessage.delete();
        }
        PlaylistMessage newPlaylistMessage = new PlaylistMessage(guildAttributesService, playlist, member.getUser());
        newPlaylistMessage.update(member.getGuild());
        playlistIdToMessage.put(member, newPlaylistMessage);
    }

    public void nextPage(Member member) {
        PlaylistMessage playlistMessage = playlistIdToMessage.get(member);
        playlistMessage.nextPage(member.getGuild());
    }

    public void previousPage(Member member) {
        PlaylistMessage playlistMessage = playlistIdToMessage.get(member);
        playlistMessage.previousPage(member.getGuild());
    }
}
