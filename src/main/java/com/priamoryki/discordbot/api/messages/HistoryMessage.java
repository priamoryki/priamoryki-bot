package com.priamoryki.discordbot.api.messages;

import com.priamoryki.discordbot.api.audio.GuildMusicManager;
import com.priamoryki.discordbot.api.common.GuildAttributesService;
import com.priamoryki.discordbot.common.Utils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Pavel Lymar
 */
public class HistoryMessage implements UsefulMessage {
    private static final int HISTORY_MAX_SIZE = 10;
    private final GuildMusicManager guildMusicManager;
    private final GuildAttributesService guildAttributesService;
    private final Deque<AudioTrack> history;

    public HistoryMessage(GuildMusicManager guildMusicManager, GuildAttributesService guildAttributesService) {
        this.guildMusicManager = guildMusicManager;
        this.guildAttributesService = guildAttributesService;
        this.history = new ArrayDeque<>();
    }

    public void put(AudioTrack track) {
        history.addFirst(track);
        if (history.size() > HISTORY_MAX_SIZE) {
            history.removeLast();
        }
    }

    private void createNewMessage() {
        guildAttributesService
                .getOrCreateMainChannel(guildMusicManager.getGuild())
                .sendMessage(fillBuilder(new MessageCreateBuilder()).build()).complete();
    }

    @Override
    public void update() {
        createNewMessage();
    }

    private <T, R extends AbstractMessageBuilder<T, R>> AbstractMessageBuilder<T, R> fillBuilder(
            AbstractMessageBuilder<T, R> messageBuilder
    ) {
        List<AudioTrack> queue = new ArrayList<>(history);
        String content = "__History__:\n" +
                IntStream.range(0, queue.size())
                        .mapToObj(
                                i -> String.format("%d) %s", i + 1, Utils.audioTrackToString(queue.get(i)))
                        ).collect(Collectors.joining("\n"));
        return messageBuilder.setContent(content);
    }
}
