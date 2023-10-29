package com.priamoryki.discordbot.repositories;

import com.priamoryki.discordbot.entities.ServerInfo;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

import static com.priamoryki.discordbot.utils.Utils.UPDATED_PROPERTY;

/**
 * @author Pavel Lymar
 */
@Repository
public class ServersRepository extends AbstractRepository<ServerInfo> {
    public ServersRepository(EntityManager manager) {
        super(manager);
    }

    public List<Long> getAllServersIds() {
        return manager.createQuery("SELECT serverId FROM ServerInfo", Long.class).getResultList();
    }

    public ServerInfo getServerById(Long id) {
        return manager.find(ServerInfo.class, id);
    }
}
