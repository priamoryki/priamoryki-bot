package com.priamoryki.discordbot.configs;

import com.priamoryki.discordbot.utils.sync.FileLoader;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;

/**
 * @author Michael Ruzavin
 */
@Configuration
public class EntityConfig {
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            FileLoader fileLoader,
            EntityManagerFactoryBuilder builder,
            DataSource dataSource
    ) {
        fileLoader.upload();

        return builder
                .dataSource(dataSource)
                .packages("com.priamoryki.discordbot.entities")
                .build();
    }
}
