package com.priamoryki.discordbot.api.database.repositories;

import com.priamoryki.discordbot.api.database.entities.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Pavel Lymar
 */
@Repository
public interface PlaylistsRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUserId(Long userId);
}
