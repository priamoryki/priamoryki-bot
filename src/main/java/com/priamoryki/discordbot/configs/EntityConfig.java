package com.priamoryki.discordbot.configs;

import com.priamoryki.discordbot.utils.sync.FileLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

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
