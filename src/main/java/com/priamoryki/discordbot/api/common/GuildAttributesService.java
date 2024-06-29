package com.priamoryki.discordbot.api.common;

import com.priamoryki.discordbot.api.database.entities.ServerInfo;
import com.priamoryki.discordbot.api.database.repositories.ServersRepository;
import com.priamoryki.discordbot.api.messages.MainMessage;
import com.priamoryki.discordbot.api.messages.PlayerMessage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.function.Function;

/**
 * @author Pavel Lymar
 */
@Service
public class GuildAttributesService {
    private static final long INVALID_ID = -1;
    private final ServersRepository serversRepository;
    @Value("${bot.name}")
    private String botName;

    public GuildAttributesService(ServersRepository serversRepository) {
        this.serversRepository = serversRepository;
    }

    public long getMainChannelId(long guildId) {
        return getIdFieldByGuildId(guildId, ServerInfo::getChannelId);
    }

    public long getMainMessageId(long guildId) {
        return getIdFieldByGuildId(guildId, ServerInfo::getMessageId);
    }

    public long getPlayerMessageId(long guildId) {
        return getIdFieldByGuildId(guildId, ServerInfo::getPlayerMessageId);
    }

    public void createGuildAttributes(Guild guild) {
        ServerInfo serverInfo = getServerInfo(guild.getIdLong());
        getOrCreateMainMessage(guild, serverInfo);
        getOrCreatePlayerMessage(guild, serverInfo);
    }

    public MessageChannel getOrCreateMainChannel(Guild guild) {
        ServerInfo serverInfo = getServerInfo(guild.getIdLong());
        return getOrCreateMainChannel(guild, serverInfo);
    }

    public Message getOrCreateMainMessage(Guild guild) {
        ServerInfo serverInfo = getServerInfo(guild.getIdLong());
        return getOrCreateMainMessage(guild, serverInfo);
    }

    public Message getOrCreatePlayerMessage(Guild guild) {
        ServerInfo serverInfo = getServerInfo(guild.getIdLong());
        return getOrCreatePlayerMessage(guild, serverInfo);
    }

    public void sendNotification(Guild guild, String text) {
        MessageChannel channel = getOrCreateMainChannel(guild);
        var messageCreateBuilder = new MessageCreateBuilder();
        var embedBuilder = new EmbedBuilder().setColor(Color.YELLOW).setTitle("BOT NEWS").setDescription(text).build();
        messageCreateBuilder.setEmbeds(
                embedBuilder
        );
        channel.sendMessage(messageCreateBuilder.build()).queue();
    }

    private MessageChannel getOrCreateMainChannel(Guild guild, ServerInfo serverInfo) {
        long mainChannelId = getOrDefaultId(serverInfo.getChannelId());
        MessageChannel channel = guild.getTextChannelById(mainChannelId);
        if (channel == null) {
            channel = guild.createTextChannel(
                    botName
            ).complete();
            serverInfo.setChannelId(channel.getIdLong());
            serversRepository.save(serverInfo);
        }
        return channel;
    }

    private Message getOrCreateMainMessage(Guild guild, ServerInfo serverInfo) {
        long mainMessageId = getOrDefaultId(serverInfo.getMessageId());
        MessageChannel channel = getOrCreateMainChannel(guild, serverInfo);
        Message message = getMessageById(channel, mainMessageId);
        if (message == null) {
            message = channel.sendMessage(
                    MainMessage.fillWithDefaultMessage(new MessageCreateBuilder()).build()
            ).complete();
            serverInfo.setMessageId(message.getIdLong());
            serversRepository.save(serverInfo);
            message.pin().complete();
        }
        return message;
    }

    private Message getOrCreatePlayerMessage(Guild guild, ServerInfo serverInfo) {
        long playerMessageId = getOrDefaultId(serverInfo.getPlayerMessageId());
        MessageChannel channel = getOrCreateMainChannel(guild, serverInfo);
        Message message = getMessageById(channel, playerMessageId);
        if (message == null) {
            message = channel.sendMessage(
                    PlayerMessage.fillWithDefaultMessage(new MessageCreateBuilder()).build()
            ).complete();
            serverInfo.setPlayerMessageId(message.getIdLong());
            serversRepository.save(serverInfo);
            message.pin().complete();
        }
        return message;
    }

    private long getOrDefaultId(Long id) {
        return id != null ? id : INVALID_ID;
    }

    private long getIdFieldByGuildId(long guildId, Function<ServerInfo, Long> mapper) {
        return serversRepository.findById(guildId).map(mapper).orElse(INVALID_ID);
    }

    private Message getMessageById(MessageChannel channel, long messageId) {
        MessageHistory messageHistory = MessageHistory.getHistoryFromBeginning(channel).complete();
        return messageHistory.getMessageById(messageId);
    }

    private ServerInfo getServerInfo(long guildId) {
        return serversRepository.findById(guildId).orElseGet(() -> {
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setServerId(guildId);
            return serversRepository.save(serverInfo);
        });
    }
}
