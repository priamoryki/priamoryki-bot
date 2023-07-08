package com.priamoryki.discordbot.utils;

import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.commands.CommandsStorage;
import com.priamoryki.discordbot.entities.ServerInfo;
import com.priamoryki.discordbot.events.EventsListener;
import com.priamoryki.discordbot.repositories.ServersRepository;
import com.priamoryki.discordbot.utils.messages.MainMessage;
import com.priamoryki.discordbot.utils.messages.PlayerMessage;
import com.priamoryki.discordbot.utils.sync.SyncService;
import com.priamoryki.discordbot.utils.sync.YaDiskSyncService;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Lymar
 */
public class DataSource {
    private final long INVALID_ID = -1;
    private final String TOKEN_ENV_NAME = "TOKEN";
    private final String YADISK_TOKEN_ENV_NAME = "YADISK_TOKEN";
    private final String SETTINGS_PATH = "data/config.json";
    private final String DB_LOCAL_PATH = "data/servers.db";
    private final String DB_CLOUD_PATH = "servers.db";
    private final ServersRepository serversRepository;
    private final JSONObject settings;
    private final SyncService syncService;
    private JDA bot;

    public DataSource() throws IOException, JSONException {
        this.settings = new JSONObject(
                new String(Files.readAllBytes(Paths.get(SETTINGS_PATH)))
        );
        this.syncService = new YaDiskSyncService(
                DB_LOCAL_PATH,
                DB_CLOUD_PATH,
                new RestClient(new Credentials("me", getYaDiskToken()))
        );
        this.syncService.load();
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("main");
        EntityManager entityManager = factory.createEntityManager();
        this.serversRepository = new ServersRepository(entityManager);
    }

    public void setupBot(CommandsStorage commands) {
        this.bot = JDABuilder.createDefault(getToken()).enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new EventsListener(this, commands))
                .build();
        List<SlashCommandData> result = new ArrayList<>();
        commands.getCommands().stream().filter(Command::isAvailableFromChat).forEach(
                command -> command.getNames().forEach(
                        name -> result.add(Utils.commandToSlashCommand(name, command))
                )
        );
        bot.updateCommands().addCommands(result).queue();
    }

    private String parseSetting(String setting) {
        try {
            return settings.getString(setting);
        } catch (JSONException jsonException) {
            return null;
        }
    }

    public String getToken() {
        return System.getenv(TOKEN_ENV_NAME);
    }

    public String getBotName() {
        return parseSetting("bot");
    }

    public long getBotId() {
        return getBot().getSelfUser().getIdLong();
    }

    public String getPrefix() {
        return parseSetting("prefix");
    }

    public String getYaDiskToken() {
        return System.getenv(YADISK_TOKEN_ENV_NAME);
    }

    public ServersRepository getServersRepository() {
        return serversRepository;
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

    public MessageChannel getOrCreateMainChannel(Guild guild) {
        MessageChannel channel = guild.getTextChannelById(getMainChannelId(guild.getIdLong()));
        if (channel == null) {
            channel = guild.createTextChannel(
                    getBotName()
            ).complete();
            ServerInfo serverInfo = serversRepository.getServerById(guild.getIdLong());
            serverInfo.setChannelId(channel.getIdLong());
            serversRepository.update(serverInfo);
            syncService.upload();
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
            syncService.upload();
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
            syncService.upload();
        }
        return message;
    }

    public boolean isBot(User user) {
        return user.getIdLong() == getBotId();
    }

    public JDA getBot() {
        return bot;
    }
}
