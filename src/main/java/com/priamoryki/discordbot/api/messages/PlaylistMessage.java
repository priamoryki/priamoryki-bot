package com.priamoryki.discordbot.api.messages;

import com.priamoryki.discordbot.api.common.GuildAttributesService;
import com.priamoryki.discordbot.api.database.entities.Playlist;
import com.priamoryki.discordbot.api.database.entities.PlaylistSong;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Pavel Lymar
 */
public class PlaylistMessage {
    private static final int MAX_SONGS_NUMBER = 20;
    private final GuildAttributesService guildAttributesService;
    private final Playlist playlist;
    private final User user;
    private Message message;
    private int page = 1;

    public PlaylistMessage(GuildAttributesService guildAttributesService, Playlist playlist, User user) {
        this.guildAttributesService = guildAttributesService;
        this.playlist = playlist;
        this.user = user;
    }

    private static List<Button> getButtons() {
        return List.of(
                Button.primary("PLAYLIST_PREVIOUS_PAGE", Emoji.fromUnicode("⏪")),
                Button.primary("PLAYLIST_NEXT_PAGE", Emoji.fromUnicode("⏩"))
        );
    }

    private void createNewMessage(Guild guild) {
        message = guildAttributesService.getOrCreateMainChannel(guild)
                .sendMessage(fillBuilder(new MessageCreateBuilder(), playlist.getSongs()).build()).complete();
    }

    public void nextPage(Guild guild) {
        page++;
        update(guild);
    }

    public void previousPage(Guild guild) {
        page--;
        update(guild);
    }

    public void update(Guild guild) {
        List<PlaylistSong> queue = playlist.getSongs();
        int lastSongNumber = MAX_SONGS_NUMBER * (page - 1);
        if (0 > lastSongNumber) {
            page = 1;
        }
        if (lastSongNumber > queue.size()) {
            page = queue.size() / MAX_SONGS_NUMBER + 1;
        }
        try {
            message.editMessage(fillBuilder(new MessageEditBuilder(), queue).build()).complete();
        } catch (Exception ignored) {
            createNewMessage(guild);
        }
    }

    public void delete() {
        message.delete().queue();
    }

    private <T, R extends AbstractMessageBuilder<T, R>> AbstractMessageBuilder<T, R> fillBuilder(
            AbstractMessageBuilder<T, R> messageBuilder, List<PlaylistSong> queue
    ) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.GREEN);
        builder.setAuthor(String.format("%s playlist", user.getName()), null, user.getAvatarUrl());
        builder.setTitle(String.format("__**%s (id=%d)**__", playlist.getName(), playlist.getId()).toUpperCase());
        IntStream.range(MAX_SONGS_NUMBER * (page - 1), Math.min(MAX_SONGS_NUMBER * page, queue.size())).forEach(
                i -> builder.addField(String.format(
                        "**%d) %s**",
                        i + 1,
                        queue.get(i).getName()
                ), "\u200e", false)
        );
        builder.setFooter(String.format(
                "Page %d / %d",
                page,
                queue.size() / MAX_SONGS_NUMBER + 1
        ));
        builder.setTimestamp(Instant.now());
        return messageBuilder.setEmbeds(builder.build()).setComponents(ActionRow.of(getButtons()));
    }
}
