package com.priamoryki.discordbot.api.database.repositories;

import com.priamoryki.discordbot.api.database.entities.YoutubeAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Pavel Lymar
 */
@Repository
public interface YoutubeAuthRepository extends JpaRepository<YoutubeAuth, Long> {

}
