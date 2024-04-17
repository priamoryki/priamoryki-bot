package com.priamoryki.discordbot.api.playlist;

import com.priamoryki.discordbot.api.database.entities.Playlist;
import com.priamoryki.discordbot.api.database.entities.PlaylistSong;
import com.priamoryki.discordbot.api.database.repositories.PlaylistsRepository;
import com.priamoryki.discordbot.commands.CommandException;
import com.priamoryki.discordbot.common.Utils;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Lymar
 */
@Service
public class UserPlaylistEditor {
    private final PlaylistsRepository playlistsRepository;
    private final Map<User, Long> userPlaylistToEdit;

    public UserPlaylistEditor(PlaylistsRepository playlistsRepository) {
        this.playlistsRepository = playlistsRepository;
        this.userPlaylistToEdit = new HashMap<>();
    }

    public Playlist getPlaylist(User user, Long id) throws CommandException {
        if (id == null) {
            throw new CommandException("You aren't editing any playlist now!");
        }
        Playlist playlist = playlistsRepository.getPlaylistById(id);
        if (playlist == null) {
            throw new CommandException("Can't find playlist with such id!");
        }
        if (playlist.getUserId() != user.getIdLong()) {
            throw new CommandException("You don't have permissions to playlist with such id!");
        }
        return playlist;
    }

    private Playlist getPlaylistToEdit(User user) throws CommandException {
        Long id = userPlaylistToEdit.get(user);
        return getPlaylist(user, id);
    }

    public void createPlaylist(User user, String name) {
        Playlist playlist = new Playlist();
        playlist.setUserId(user.getIdLong());
        playlist.setName(name);
        playlist.setSongs(new LinkedList<>());
        playlistsRepository.update(playlist);
    }

    public void changePlaylistToEdit(User user, Long id) throws CommandException {
        Playlist playlist = getPlaylist(user, id);
        userPlaylistToEdit.put(user, playlist.getId());
    }

    public void addSongs(User user, Collection<SongInfo> songs) throws CommandException {
        Playlist playlist = getPlaylistToEdit(user);
        if (playlist == null) {
            throw new CommandException("Can't find playlist with such id!");
        }
        List<PlaylistSong> playlistSongs = songs.stream().map(info -> {
            PlaylistSong song = new PlaylistSong();
            song.setName(info.name());
            song.setUrl(info.url());
            return song;
        }).toList();
        playlistsRepository.addSongs(playlist, playlistSongs);
    }

    public void removeSongs(User user, int from, int to) throws CommandException {
        Playlist playlist = getPlaylistToEdit(user);
        LinkedList<PlaylistSong> songs = new LinkedList<>(playlist.getSongs());
        Utils.validateBounds(from, to, songs.size(), "Can't remove interval that isn't in playlist!");
        playlist.getSongs().subList(from - 1, to).clear();
    }

    public void deletePlaylist(User user, Long id) throws CommandException {
        Playlist playlist = getPlaylist(user, id);
        playlistsRepository.remove(playlist);
    }

    public List<Playlist> getUserPlaylists(User user) {
        return playlistsRepository.getPlaylistsByUserId(user.getIdLong());
    }
}
