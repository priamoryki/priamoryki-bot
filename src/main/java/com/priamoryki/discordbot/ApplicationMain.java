package com.priamoryki.discordbot;

import com.priamoryki.discordbot.api.database.repositories.CommonRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Michael Ruzavin
 */
@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(repositoryBaseClass = CommonRepositoryImpl.class)
public class ApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationMain.class, args);
    }
}
