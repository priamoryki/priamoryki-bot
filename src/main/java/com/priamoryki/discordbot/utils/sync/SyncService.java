package com.priamoryki.discordbot.utils.sync;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * @author Michael Ruzavin
 */
@Service
public class SyncService {
    private final FileLoader fileLoader;

    public SyncService(FileLoader fileLoader) {
        this.fileLoader = fileLoader;
    }

    @Scheduled(fixedRate = 10_000)
    public void uploadFileIfUpdated() {
        if (UpdatePlannedProperty.getPlanned()) {
            fileLoader.upload();
            UpdatePlannedProperty.setPlanned(false);
        }
    }
}
