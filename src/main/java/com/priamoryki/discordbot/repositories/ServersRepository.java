package com.priamoryki.discordbot.repositories;

import com.priamoryki.discordbot.entities.ServerInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Pavel Lymar
 */
@Repository
public interface ServersRepository extends JpaRepository<ServerInfo, Long> {

}
