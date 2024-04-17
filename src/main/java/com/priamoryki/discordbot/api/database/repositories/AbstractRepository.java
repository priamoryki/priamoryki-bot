package com.priamoryki.discordbot.api.database.repositories;

import com.priamoryki.discordbot.common.sync.UpdatePlannedProperty;

import jakarta.persistence.EntityManager;

/**
 * @author Pavel Lymar
 */
public abstract class AbstractRepository<T> {
    protected final EntityManager manager;

    protected AbstractRepository(EntityManager manager) {
        this.manager = manager;
    }

    public void update(T entity) {
        manager.getTransaction().begin();
        manager.persist(entity);
        manager.getTransaction().commit();
        UpdatePlannedProperty.setPlanned(true);
    }
}
