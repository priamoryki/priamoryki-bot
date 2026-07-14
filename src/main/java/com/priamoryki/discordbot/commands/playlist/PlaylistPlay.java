package com.priamoryki.discordbot.commands.playlist;

import com.priamoryki.discordbot.api.audio.MusicManager;
import com.priamoryki.discordbot.api.audio.SongRequest;
import com.priamoryki.discordbot.api.database.entities.Playlist;
import com.priamoryki.discordbot.api.database.entities.PlaylistSong;
import com.priamoryki.discordbot.api.playlist.UserPlaylistEditor;
import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.commands.MusicCommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class PlaylistPlay extends MusicCommand {
    private final UserPlaylistEditor userPlaylistEditor;

    public PlaylistPlay(MusicManager musicManager, UserPlaylistEditor userPlaylistEditor) {
        super(musicManager);
        this.userPlaylistEditor = userPlaylistEditor;
    }

    @Override
    public List<String> getNames() {
        return List.of("playlist_play");
    }

    @Override
    public String getDescription() {
        return "Adds music from playlist to the queue";
    }

    @Override
    public List<OptionData> getOptions() {
        return List.of(
                new OptionData(OptionType.INTEGER, "playlist_id", "Playlist id", true)
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
        Long id = Long.parseLong(args.get(0));
        Playlist playlist = userPlaylistEditor.getPlaylist(member.getUser(), id);
        List<PlaylistSong> songs = playlist.getSongs();
        musicManager.getGuildMusicManager(guild).join(member);
        for (PlaylistSong song : songs) {
            musicManager.getGuildMusicManager(guild).play(new SongRequest(guild, member, song.getUrl()));
        }
    }
}
