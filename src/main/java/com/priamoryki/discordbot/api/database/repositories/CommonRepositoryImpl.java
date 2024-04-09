package com.priamoryki.discordbot.api.database.repositories;

import com.priamoryki.discordbot.common.sync.UpdatePlannedProperty;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import jakarta.persistence.EntityManager;

/**
 * @author Pavel Lymar
 */
public class CommonRepositoryImpl<T, R> extends SimpleJpaRepository<T, R> {
    public CommonRepositoryImpl(
            JpaEntityInformation<T, ?> entityInformation,
            EntityManager entityManager
    ) {
        super(entityInformation, entityManager);
    }

    @Override
    public <S extends T> S save(S entity) {
        UpdatePlannedProperty.setPlanned(true);
        return super.save(entity);
    }
}
