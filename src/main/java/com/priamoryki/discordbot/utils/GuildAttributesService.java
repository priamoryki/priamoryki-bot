package com.priamoryki.discordbot.utils;

import com.priamoryki.discordbot.entities.ServerInfo;
import com.priamoryki.discordbot.repositories.ServersRepository;
import com.priamoryki.discordbot.utils.messages.MainMessage;
import com.priamoryki.discordbot.utils.messages.PlayerMessage;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
        ServerInfo serverInfo = serversRepository.getServerById(guildId);
        if (serverInfo == null || serverInfo.getChannelId() == null) {
            return INVALID_ID;
        }
        return serverInfo.getChannelId();
    }

    public long getMainMessageId(long guildId) {
        ServerInfo serverInfo = serversRepository.getServerById(guildId);
        if (serverInfo == null || serverInfo.getMessageId() == null) {
            return INVALID_ID;
        }
        return serverInfo.getMessageId();
    }

    public long getPlayerMessageId(long guildId) {
        ServerInfo serverInfo = serversRepository.getServerById(guildId);
        if (serverInfo == null || serverInfo.getPlayerMessageId() == null) {
            return INVALID_ID;
        }
        return serverInfo.getPlayerMessageId();
    }

    public void createGuildAttributes(Guild guild) {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setServerId(guild.getIdLong());
        serversRepository.update(serverInfo);
        getOrCreateMainMessage(guild);
        getOrCreatePlayerMessage(guild);
    }

    public MessageChannel getOrCreateMainChannel(Guild guild) {
        MessageChannel channel = guild.getTextChannelById(getMainChannelId(guild.getIdLong()));
        if (channel == null) {
            channel = guild.createTextChannel(
                    botName
            ).complete();
            ServerInfo serverInfo = serversRepository.getServerById(guild.getIdLong());
            serverInfo.setChannelId(channel.getIdLong());
            serversRepository.update(serverInfo);
        }
        return channel;
    }

    public Message getOrCreateMainMessage(Guild guild) {
        MessageChannel channel = getOrCreateMainChannel(guild);
        Message message = MessageHistory.getHistoryFromBeginning(channel).complete()
                .getMessageById(getMainMessageId(guild.getIdLong()));
        if (message == null) {
            message = channel.sendMessage(
                    MainMessage.fillWithDefaultMessage(new MessageCreateBuilder()).build()
            ).complete();
            ServerInfo serverInfo = serversRepository.getServerById(guild.getIdLong());
            serverInfo.setMessageId(message.getIdLong());
            serversRepository.update(serverInfo);
            message.pin().complete();
        }
        return message;
    }

    public Message getOrCreatePlayerMessage(Guild guild) {
        MessageChannel channel = getOrCreateMainChannel(guild);
        Message message = MessageHistory.getHistoryFromBeginning(channel).complete()
                .getMessageById(getPlayerMessageId(guild.getIdLong()));
        if (message == null) {
            message = channel.sendMessage(
                    PlayerMessage.fillWithDefaultMessage(new MessageCreateBuilder()).build()
            ).complete();
            ServerInfo serverInfo = serversRepository.getServerById(guild.getIdLong());
            serverInfo.setPlayerMessageId(message.getIdLong());
            serversRepository.update(serverInfo);
            message.pin().complete();
        }
        return message;
    }
}
