package com.priamoryki.discordbot.repositories;

import com.priamoryki.discordbot.entities.ServerInfo;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Lymar
 */
public class ServersRepository {
    private final EntityManager manager;

    public ServersRepository(EntityManager manager) {
        this.manager = manager;
    }

    public List<Long> getAllServersIds() {
        List<Long> result = new ArrayList<>();
        for (var i : manager.createQuery("SELECT serverId FROM ServerInfo").getResultList()) {
            result.add((Long) i);
        }
        return result;
    }

    public ServerInfo getServerById(Long id) {
        return manager.find(ServerInfo.class, id);
    }

    public void update(ServerInfo serverInfo) {
        manager.getTransaction().begin();
        manager.persist(serverInfo);
        manager.getTransaction().commit();
    }
}
