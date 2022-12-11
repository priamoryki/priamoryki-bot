package com.priamoryki.discordbot.utils;

import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.commands.CommandsStorage;
import com.priamoryki.discordbot.events.EventsListener;
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
import org.apache.hc.core5.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Pavel Lymar
 */
public class DataSource {
    private final int INVALID_ID = -1;
    private final String TOKEN_ENV_NAME = "TOKEN";
    private final String YADISK_TOKEN_ENV_NAME = "YADISK_TOKEN";
    private final String SPOTIFY_CLIENT_ID_ENV_NAME = "SPOTIFY_CLIENT_ID";
    private final String SPOTIFY_CLIENT_SECRET_ENV_NAME = "SPOTIFY_CLIENT_SECRET";
    private final String SETTINGS_PATH = "data/config.json";
    private final String DB_LOCAL_PATH = "data/servers.db";
    private final String DB_PATH = "jdbc:sqlite:" + DB_LOCAL_PATH;
    private final String MAIN_CHANNEL_ID_TOKEN = "channel_id";
    private final String MAIN_MESSAGE_ID_TOKEN = "message_id";
    private final String PLAYER_MESSAGE_ID_TOKEN = "player_message_id";
    private final Connection connection;
    private final JSONObject settings;
    private final SyncService syncService;
    private final SpotifyApi spotifyApi;
    private JDA bot;

    public DataSource() throws SQLException, IOException, JSONException {
        this.settings = new JSONObject(
                new String(Files.readAllBytes(Paths.get(SETTINGS_PATH)))
        );
        this.syncService = new YaDiskSyncService(
                DB_LOCAL_PATH,
                "servers.db",
                new RestClient(new Credentials("me", getYaDiskToken()))
        );
        this.syncService.load();
        this.connection = DriverManager.getConnection(DB_PATH);
        connection.setAutoCommit(true);
        this.spotifyApi = SpotifyApi.builder()
                .setClientId(getSpotifyClientId())
                .setClientSecret(getSpotifyClientSecret())
                .build();
        updateSpotifyApi();
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
        try {
            return settings.getLong("id");
        } catch (JSONException e) {
            return INVALID_ID;
        }
    }

    public String getPrefix() {
        return parseSetting("prefix");
    }

    public String getYaDiskToken() {
        return System.getenv(YADISK_TOKEN_ENV_NAME);
    }

    public String getSpotifyClientId() {
        return System.getenv(SPOTIFY_CLIENT_ID_ENV_NAME);
    }

    public String getSpotifyClientSecret() {
        return System.getenv(SPOTIFY_CLIENT_SECRET_ENV_NAME);
    }

    private void updateSpotifyApi() {
        try {
            spotifyApi.setAccessToken(spotifyApi.clientCredentials().build().execute().getAccessToken());
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            System.err.println("SpotifyApi login error: " + e.getMessage());
        }
    }

    public ResultSet executeQuery(String query) {
        try {
            return connection.createStatement().executeQuery(query);
        } catch (SQLException e) {
            return null;
        }
    }

    public List<Long> getAllGuildIds() {
        ResultSet queryResult = executeQuery("SELECT server_id from servers");
        List<Long> result = new ArrayList<>();
        try {
            while (queryResult.next()) {
                result.add(queryResult.getLong("server_id"));
            }
        } catch (SQLException ignored) {
        }
        return result;
    }

    private ResultSet getFieldByGuild(String field, long guildId) {
        return executeQuery(String.format("SELECT %s FROM servers WHERE server_id = %d", field, guildId));
    }

    public long getMainChannelId(long guildId) {
        ResultSet result = getFieldByGuild(MAIN_CHANNEL_ID_TOKEN, guildId);
        try {
            return Objects.requireNonNull(result).getLong(MAIN_CHANNEL_ID_TOKEN);
        } catch (Exception e) {
            return INVALID_ID;
        }
    }

    public long getMainMessageId(long guildId) {
        ResultSet result = getFieldByGuild(MAIN_MESSAGE_ID_TOKEN, guildId);
        try {
            return Objects.requireNonNull(result).getLong(MAIN_MESSAGE_ID_TOKEN);
        } catch (Exception e) {
            return INVALID_ID;
        }
    }

    public long getPlayerMessageId(long guildId) {
        ResultSet result = getFieldByGuild(PLAYER_MESSAGE_ID_TOKEN, guildId);
        try {
            return Objects.requireNonNull(result).getLong(PLAYER_MESSAGE_ID_TOKEN);
        } catch (Exception e) {
            return INVALID_ID;
        }
    }

    public MessageChannel getOrCreateMainChannel(Guild guild) {
        MessageChannel channel = guild.getTextChannelById(getMainChannelId(guild.getIdLong()));
        if (channel == null) {
            channel = guild.createTextChannel(getBotName()).complete();
            executeQuery(
                    String.format(
                            "UPDATE servers SET channel_id = %d WHERE server_id = %d",
                            channel.getIdLong(), guild.getIdLong()
                    )
            );
            syncService.upload();
        }
        return channel;
    }

    public Message getOrCreateMainMessage(Guild guild) {
        MessageChannel channel = getOrCreateMainChannel(guild);
        Message message = MessageHistory.getHistoryFromBeginning(channel).complete()
                .getMessageById(getMainMessageId(guild.getIdLong()));
        if (message == null) {
            message = channel.sendMessage(MainMessage.fillWithDefaultMessage(new MessageCreateBuilder()).build()).complete();
            executeQuery(
                    String.format(
                            "UPDATE servers SET message_id = %d WHERE server_id = %d",
                            message.getIdLong(), guild.getIdLong()
                    )
            );
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
            executeQuery(
                    String.format(
                            "UPDATE servers SET player_message_id = %d WHERE server_id = %d",
                            message.getIdLong(), guild.getIdLong()
                    )
            );
            message.pin().complete();
            syncService.upload();
        }
        return message;
    }

    public boolean isBot(User user) {
        return user.getIdLong() == getBotId();
    }

    public SpotifyApi getSpotifyApi() {
        // TODO pretty long request
        updateSpotifyApi();
        return spotifyApi;
    }

    public JDA getBot() {
        return bot;
    }
}
