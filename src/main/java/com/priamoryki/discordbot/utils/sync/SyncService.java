package com.priamoryki.discordbot.utils.sync;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;

import static com.priamoryki.discordbot.utils.Utils.UPDATED_PROPERTY;

@Service
public class SyncService {
    private final EntityManager entityManager;
    private final FileLoader loader;

    public SyncService(EntityManager entityManager, FileLoader loader) {
        this.loader = loader;
        this.entityManager = entityManager;
    }

    @Scheduled(fixedRate = 10_000)
    public void uploadFileIfUpdated() {
        var value = entityManager.getProperties().get(UPDATED_PROPERTY);
        if (value == null) {
            return;
        }
        if (Boolean.TRUE.equals(value)) {
            loader.upload();
            entityManager.setProperty(UPDATED_PROPERTY, false);
        }
    }
}
