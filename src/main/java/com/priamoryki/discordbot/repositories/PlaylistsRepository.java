package com.priamoryki.discordbot.repositories;

import com.priamoryki.discordbot.entities.Playlist;
import com.priamoryki.discordbot.entities.PlaylistSong;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Pavel Lymar
 */
@Repository
public class PlaylistsRepository {
    private final EntityManager manager;

    public PlaylistsRepository(EntityManager manager) {
        this.manager = manager;
    }

    public Playlist getPlaylistById(Long id) {
        return manager.find(Playlist.class, id);
    }

    public List<Playlist> getPlaylistsByUserId(Long id) {
        return manager.createQuery("FROM Playlist WHERE userId=:user_id", Playlist.class)
                .setParameter("user_id", id).getResultList();
    }

    public void addSongs(Playlist playlist, Collection<PlaylistSong> songs) {
        manager.getTransaction().begin();
        playlist.getSongs().addAll(songs);
        songs.forEach(manager::persist);
        manager.persist(playlist);
        manager.getTransaction().commit();
    }

    public void remove(Playlist playlist) {
        manager.getTransaction().begin();
        manager.remove(playlist);
        manager.getTransaction().commit();
    }

    public void update(Playlist playlist) {
        manager.getTransaction().begin();
        manager.persist(playlist);
        manager.getTransaction().commit();
    }
}
