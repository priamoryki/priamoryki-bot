package com.priamoryki.discordbot.api.common;

import com.priamoryki.discordbot.api.database.entities.ServerInfo;
import com.priamoryki.discordbot.api.database.repositories.ServersRepository;
import com.priamoryki.discordbot.api.messages.MainMessage;
import com.priamoryki.discordbot.api.messages.PlayerMessage;
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
        return serversRepository.findById(guildId).map(ServerInfo::getChannelId).orElse(INVALID_ID);
    }

    public long getMainMessageId(long guildId) {
        return serversRepository.findById(guildId).map(ServerInfo::getMessageId).orElse(INVALID_ID);
    }

    public long getPlayerMessageId(long guildId) {
        return serversRepository.findById(guildId).map(ServerInfo::getPlayerMessageId).orElse(INVALID_ID);
    }

    public void createGuildAttributes(Guild guild) {
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setServerId(guild.getIdLong());
        serversRepository.save(serverInfo);
        getOrCreateMainMessage(guild);
        getOrCreatePlayerMessage(guild);
    }

    public MessageChannel getOrCreateMainChannel(Guild guild) {
        MessageChannel channel = guild.getTextChannelById(getMainChannelId(guild.getIdLong()));
        if (channel == null) {
            channel = guild.createTextChannel(
                    botName
            ).complete();
            ServerInfo serverInfo = serversRepository.findById(guild.getIdLong()).get();
            serverInfo.setChannelId(channel.getIdLong());
            serversRepository.save(serverInfo);
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
            ServerInfo serverInfo = serversRepository.findById(guild.getIdLong()).get();
            serverInfo.setMessageId(message.getIdLong());
            serversRepository.save(serverInfo);
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
            ServerInfo serverInfo = serversRepository.findById(guild.getIdLong()).get();
            serverInfo.setPlayerMessageId(message.getIdLong());
            serversRepository.save(serverInfo);
            message.pin().complete();
        }
        return message;
    }
}
