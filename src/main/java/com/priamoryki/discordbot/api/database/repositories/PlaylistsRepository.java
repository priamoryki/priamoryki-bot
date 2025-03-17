package com.priamoryki.discordbot.api.database.repositories;

import com.priamoryki.discordbot.api.database.entities.Playlist;
import com.priamoryki.discordbot.api.database.entities.PlaylistSong;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.Collection;
import java.util.List;

/**
 * @author Pavel Lymar
 */
@Repository
public class PlaylistsRepository extends AbstractRepository<Playlist> {
    public PlaylistsRepository(EntityManager manager) {
        super(manager);
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
        update(playlist);
    }

    public void remove(Playlist playlist) {
        manager.getTransaction().begin();
        manager.remove(playlist);
        manager.getTransaction().commit();
        update(playlist);
    }
}
