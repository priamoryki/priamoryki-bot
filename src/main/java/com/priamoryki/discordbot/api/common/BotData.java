package com.priamoryki.discordbot.api.common;

import com.priamoryki.discordbot.api.events.EventsListener;
import com.priamoryki.discordbot.api.messages.MainMessage;
import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.commands.CommandsStorage;
import com.priamoryki.discordbot.common.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Pavel Lymar, Michael Ruzavin
 */
@Service
public class BotData {
    private final JDA bot;
    @Value("${bot.prefix}")
    private String botPrefix;
    @Value("${BOT_AUTHOR_ID}")
    private List<Long> botAuthorIds;

    public BotData(JDA bot) {
        this.bot = bot;
    }

    public void setupBot(CommandsStorage commands, EventsListener eventsListener) {
        MainMessage.fillTextField(commands);
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

    public List<Long> getBotAuthorIds() {
        return botAuthorIds;
    }

    public boolean isBot(User user) {
        return user.getIdLong() == getBotId();
    }

    public Guild getGuildById(long guildId) {
        return bot.getGuildById(guildId);
    }

    public List<Guild> getGuilds() {
        return bot.getGuilds();
    }
}
