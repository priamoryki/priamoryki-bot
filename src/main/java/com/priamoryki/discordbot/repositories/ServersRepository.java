package com.priamoryki.discordbot.repositories;

import com.priamoryki.discordbot.entities.ServerInfo;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import java.util.List;

import static com.priamoryki.discordbot.utils.Utils.UPDATED_PROPERTY;

/**
 * @author Pavel Lymar
 */
@Repository
public class ServersRepository {
    private final EntityManager manager;

    public ServersRepository(EntityManager manager) {
        this.manager = manager;
    }

    public List<Long> getAllServersIds() {
        return manager.createQuery("SELECT serverId FROM ServerInfo", Long.class).getResultList();
    }

    public ServerInfo getServerById(Long id) {
        return manager.find(ServerInfo.class, id);
    }

    public void update(ServerInfo serverInfo) {
        manager.getTransaction().begin();
        manager.persist(serverInfo);
        manager.getTransaction().commit();
        manager.setProperty(UPDATED_PROPERTY, true);
    }
}
