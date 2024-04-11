package com.priamoryki.discordbot.common.sync;

import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.DownloadListener;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Pavel Lymar, Michael Ruzavin
 */
@Service
public class YaDiskFileLoader implements FileLoader {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RestClient cloudApi;
    @Value("${db.local.path}")
    public String dbLocalPath;
    @Value("${db.server.path}")
    public String dbServerPath;

    public YaDiskFileLoader(@Value("${YADISK_TOKEN}") String yandexDiskToken) {
        this.cloudApi = new RestClient(new Credentials("me", yandexDiskToken));
    }

    @PostConstruct
    public void init() {
        load();
    }

    @Override
    public void load() {
        logger.info("Loading file {} from YaDisk", dbServerPath);
        try {
            if (!new File(dbLocalPath).delete()) {
                logger.error("Can't delete file {}", dbLocalPath);
            }
            cloudApi.downloadFile(
                    dbServerPath,
                    new File(dbLocalPath),
                    new DownloadListener() {
                        @Override
                        public OutputStream getOutputStream(boolean b) {
                            return null;
                        }
                    }
            );
        } catch (IOException | ServerException e) {
            logger.error("Error on loading file", e);
        }
    }

    @Override
    public void upload() {
        logger.info("Uploading file {} to YaDisk", dbLocalPath);
        try {
            String path = dbServerPath.substring(0, Math.max(0, dbServerPath.lastIndexOf("/")));
            if (!path.isBlank()) {
                cloudApi.makeFolder(path);
            }
        } catch (IOException | ServerException e) {
            logger.error("Can't create directory", e);
        }
        try {
            cloudApi.uploadFile(
                    cloudApi.getUploadLink(dbServerPath, true),
                    false,
                    new File(dbLocalPath),
                    new DownloadListener() {
                        @Override
                        public OutputStream getOutputStream(boolean b) {
                            return null;
                        }
                    }
            );
        } catch (IOException | ServerException e) {
            logger.error("Error on uploading file", e);
        }
    }
}
