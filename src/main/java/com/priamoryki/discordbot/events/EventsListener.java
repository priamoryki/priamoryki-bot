package com.priamoryki.discordbot.events;

import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.commands.CommandsStorage;
import com.priamoryki.discordbot.utils.DataSource;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
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
    public void onReady(@NotNull ReadyEvent event) {
        System.out.println("Bot is working now!");
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        createGuildAttributes(event.getGuild());
    }

    private void createGuildAttributes(Guild guild) {
        data.executeQuery(String.format("INSERT OR IGNORE INTO servers(server_id) VALUES (%d)", guild.getIdLong()));
        Message message = data.getOrCreateMainMessage(guild);
        data.getOrCreatePlayerMessage(guild);
        commands.executeCommand("clear_all", message.getGuild(), message.getMember());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        String messageText = message.getContentDisplay();
        // TODO вот бы это делать более обдуманно
//        if (messageText.equals("create")) {
//            createGuildAttributes(message.getGuild());
//        }
//        data.getOrCreateMainMessage(message.getGuild()).editMessage(MainMessage.getDefaultMessage()).complete();
        commands.executeCommand(
                "clear",
                message.getGuild(),
                message.getMember(),
                List.of(Long.toString(message.getIdLong()))
        );
        if (messageText.startsWith(data.getPrefix())) {
            List<String> splittedMessage = List.of(messageText.substring(data.getPrefix().length()).split(" "));
            Command command = commands.getCommand(splittedMessage.get(0));
            if (command != null && command.isAvailableFromChat()) {
                command.execute(message.getGuild(), message.getMember(), splittedMessage.subList(1, splittedMessage.size()));
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Command command = commands.getCommand(event.getName());
        if (command != null && command.isAvailableFromChat()) {
            List<String> args = new ArrayList<>();
            for (OptionData option : command.getOptions()) {
                event.getOptionsByName(option.getName()).stream()
                        .map(OptionMapping::getAsString)
                        .forEach(op -> args.addAll(Arrays.asList(op.split(" "))));
            }
            command.execute(event.getGuild(), event.getMember(), args);
        }
        event.reply("DONE!").setEphemeral(true).queue();
    }


    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        commands.executeCommand(
                Objects.requireNonNull(event.getButton().getId()).toLowerCase(),
                event.getGuild(),
                event.getMember()
        );
    }
}
