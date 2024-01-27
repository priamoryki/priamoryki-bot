package com.priamoryki.discordbot.utils;

import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.commands.CommandsStorage;
import com.priamoryki.discordbot.events.EventsListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author Pavel Lymar, Michael Ruzavin
 */
@Service
public class BotData {
    private final JDA bot;
    @Value("${bot.prefix}")
    private String botPrefix;

    public BotData(JDA bot) {
        this.bot = bot;
    }

    public void setupBot(CommandsStorage commands, EventsListener eventsListener) {
        bot.addEventListener(eventsListener);
        var result = commands.getCommands().stream()
                .filter(Command::isAvailableFromChat)
                .flatMap(
                        command -> command.getNames().stream().map(name -> Utils.commandToSlashCommand(name, command))
                )
                .toList();
        bot.updateCommands().addCommands(result).queue();
    }

    public long getBotId() {
        return bot.getSelfUser().getIdLong();
    }

    public String getPrefix() {
        return botPrefix;
    }

    public boolean isBot(User user) {
        return user.getIdLong() == getBotId();
    }
}
