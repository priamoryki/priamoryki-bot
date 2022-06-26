package com.priamoryki.discordbot.events;

import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.commands.CommandsStorage;
import com.priamoryki.discordbot.utils.DataSource;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * @author Pavel Lymar
 */
public class EventsListener extends ListenerAdapter {
    private final DataSource data;
    private final CommandsStorage commands;

    public EventsListener(DataSource data, CommandsStorage commands) {
        this.data = data;
        this.commands = commands;
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        System.out.println("Bot is working now!");
    }

    @Override
    public void onGuildJoin(@Nonnull GuildJoinEvent event) {
        createGuildAttributes(event.getGuild());
    }

    public void createGuildAttributes(Guild guild) {
        data.executeQuery(String.format("INSERT OR IGNORE INTO servers(server_id) VALUES (%d)", guild.getIdLong()));
        Message message = data.getOrCreateMainMessage(guild);
        data.getOrCreatePlayerMessage(guild);
        commands.executeCommand("clear_all", message);
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        Message message = event.getMessage();
        String messageText = message.getContentDisplay();
//        if (messageText.equals("create")) {
//            createGuildAttributes(message.getGuild());
//        }
        commands.executeCommand("clear", message);
        if (messageText.startsWith(data.getPrefix())) {
            List<String> splittedMessage = List.of(messageText.substring(data.getPrefix().length()).split(" "));
            Command command = commands.getCommand(splittedMessage.get(0));
            if (command != null && command.isAvailableFromChat()) {
                command.execute(message, splittedMessage.subList(1, splittedMessage.size()));
            }
        }
    }

    @Override
    public void onButtonInteraction(@Nonnull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        commands.executeCommand(Objects.requireNonNull(event.getButton().getId()).toLowerCase(), event.getMessage());
    }
}
