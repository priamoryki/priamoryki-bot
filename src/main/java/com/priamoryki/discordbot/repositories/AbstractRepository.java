package com.priamoryki.discordbot.repositories;

import javax.persistence.EntityManager;

import static com.priamoryki.discordbot.common.Utils.UPDATED_PROPERTY;

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
        manager.setProperty(UPDATED_PROPERTY, true);
    }
}
