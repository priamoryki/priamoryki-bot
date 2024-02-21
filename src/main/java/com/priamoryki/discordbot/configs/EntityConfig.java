package com.priamoryki.discordbot.configs;

import com.priamoryki.discordbot.utils.sync.FileLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 * @author Michael Ruzavin
 */
@Configuration
public class EntityConfig {
    @Bean
    public EntityManager getEntityManager(EntityManagerFactory factory) {
        return factory.createEntityManager();
    }

    @Bean
    public EntityManagerFactory getEntityManagerFactory(FileLoader fileLoader) {
        EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("main");
        fileLoader.upload();
        return entityManagerFactory;
    }
}
