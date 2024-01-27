package com.priamoryki.discordbot.configs;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Pavel Lymar
 */
@Configuration
public class BotConfig {
    @Bean
    public JDA getJDA(@Value("${TOKEN}") String token) {
        return JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build();
    }
}
