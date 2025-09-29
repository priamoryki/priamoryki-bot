package com.priamoryki.discordbot;

import com.priamoryki.discordbot.api.common.BotData;
import com.priamoryki.discordbot.api.events.EventsListener;
import com.priamoryki.discordbot.commands.CommandsStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Lymar, Michael Ruzavin
 */
@Service
public class Bot implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BotData data;
    private final CommandsStorage commands;
    private final EventsListener eventsListener;

    public Bot(BotData data, CommandsStorage commands, EventsListener eventsListener) {
        this.data = data;
        this.commands = commands;
        this.eventsListener = eventsListener;
    }

    public void start() {
        try {
            data.setupBot(commands, eventsListener);
        } catch (Exception e) {
            logger.error("Error on bot start", e);
        }
    }

    @Override
    public void run(String... args) {
        start();
    }
}
