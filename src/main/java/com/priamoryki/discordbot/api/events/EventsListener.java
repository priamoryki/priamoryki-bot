package com.priamoryki.discordbot.api.events;

import com.priamoryki.discordbot.api.common.BotData;
import com.priamoryki.discordbot.api.common.GuildAttributesService;
import com.priamoryki.discordbot.api.messages.MainMessage;
import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.commands.CommandsStorage;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import io.micrometer.core.instrument.util.IOUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Pavel Lymar
 */
public class EventsListener extends ListenerAdapter {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BotData data;
    private final CommandsStorage commands;
    private final GuildAttributesService guildAttributesService;

    public EventsListener(BotData data, CommandsStorage commands, GuildAttributesService guildAttributesService) {
        this.data = data;
        this.commands = commands;
        this.guildAttributesService = guildAttributesService;
    }

    private boolean isBotMentioned(Message message) {
        return message.getMentions().getUsers().stream().anyMatch(data::isBot);
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        for (Guild guild : data.getGuilds()) {
            Message message = guildAttributesService.getOrCreateMainMessage(guild);
            try {
                message.editMessage(MainMessage.fillWithDefaultMessage(new MessageEditBuilder()).build()).queue();
            } catch (Exception e) {
                logger.error("Can't update main message for guild {}: {}", guild.getName(), e.getMessage());
            }
        }
        logger.info("Bot is working now!");
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        createGuildAttributes(event.getGuild());
    }

    private void createGuildAttributes(Guild guild) {
        guildAttributesService.createGuildAttributes(guild);
        Member member = guild.getMemberById(data.getBotId());
        commands.executeCommand("clear_all", guild, member);
    }

    public void onBotAuthorMessage(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        if (isBotMentioned(message)) {
            try {
                var file = IOUtils.toString(message.getAttachments().getFirst().getProxy().download().get(), StandardCharsets.UTF_8);
                JsonBrowser json = JsonBrowser.parse(file);
                String text = json.get("message").text();
                List<Guild> guilds = json.get("guilds").values().stream()
                        .map(JsonBrowser::text)
                        .map(Long::valueOf)
                        .map(data::getGuildById)
                        .toList();
                if (guilds.isEmpty()) {
                    guilds = data.getGuilds();
                }

                for (Guild guild : guilds) {
                    guildAttributesService.sendNotification(guild, text);
                }
            } catch (Exception e) {
                logger.error("Error on sending news: {}", e.getMessage(), e);
            }
        }
    }

    public void onPrivateMessageReceived(@NotNull MessageReceivedEvent event) {
        if (data.getBotAuthorIds().contains(event.getAuthor().getIdLong())) {
            onBotAuthorMessage(event);
        }
        // LATER
    }

    public void onGuildMessageReceived(@NotNull MessageReceivedEvent event) {
        Message message = event.getMessage();
        Guild guild = event.getGuild();
        Member member = event.getMember();
        String messageText = message.getContentDisplay();
        boolean isUserBot = data.isBot(event.getAuthor());

        if (isBotMentioned(message)) {
            createGuildAttributes(guild);
        }

        boolean isBotChannel = message.getChannel().getIdLong() == guildAttributesService.getMainChannelId(guild.getIdLong());
        if (isBotChannel && !isUserBot) {
            message.delete().queue();
        }

        if (!isBotChannel || !messageText.startsWith(data.getPrefix()) || isUserBot) {
            return;
        }

        List<String> splittedMessage = List.of(messageText.substring(data.getPrefix().length()).split(" "));
        String commandName = splittedMessage.getFirst();
        commands.executeCommandWithPermissions(
                commandName,
                guild,
                member,
                splittedMessage.subList(1, splittedMessage.size()),
                () -> {
                },
                e -> message.reply(e.getMessage()).queue(),
                e -> message.reply("Something went wrong. Try again later.").queue(),
                true
        );
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            onPrivateMessageReceived(event);
        } else {
            onGuildMessageReceived(event);
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        Command command = commands.getCommand(commandName);
        long mainChannelId = guildAttributesService.getMainChannelId(event.getGuild().getIdLong());
        if (command != null && command.isAvailableFromChat() && event.getChannel().getIdLong() == mainChannelId) {
            List<String> args = command.getOptions().stream()
                    .flatMap(option -> event.getOptionsByName(option.getName()).stream())
                    .map(OptionMapping::getAsString)
                    .flatMap(op -> Arrays.stream(op.split(" ")))
                    .toList();
            commands.executeCommandWithPermissions(
                    commandName,
                    event.getGuild(),
                    event.getMember(),
                    args,
                    () -> event.reply("DONE!").setEphemeral(true).queue(),
                    e -> event.reply(e.getMessage()).setEphemeral(true).queue(),
                    e -> event.reply("Something went wrong. Try again later.").setEphemeral(true).queue(),
                    true
            );
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        event.deferEdit().queue();
        commands.executeCommandWithPermissions(
                Objects.requireNonNull(event.getButton().getId()).toLowerCase(),
                event.getGuild(),
                event.getMember(),
                () -> {
                },
                e -> event.reply(e.getMessage()).queue(),
                e -> event.reply("Something went wrong. Try again later.").setEphemeral(true).queue(),
                false
        );
    }
}
