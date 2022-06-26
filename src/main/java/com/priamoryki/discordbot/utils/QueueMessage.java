package com.priamoryki.discordbot.utils;

import com.priamoryki.discordbot.audio.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Pavel Lymar
 */
public class QueueMessage implements UsefulMessage {
    private final GuildMusicManager guildMusicManager;
    private final int MAX_SONGS_NUMBER = 20;
    private Message queueMessage;
    private int page = 1;

    public QueueMessage(GuildMusicManager guildMusicManager) {
        this.guildMusicManager = guildMusicManager;
    }

    private static List<Button> getButtons() {
        List<Button> result = new ArrayList<>();
        result.add(Button.primary("PREVIOUS_PAGE", "⏪"));
        result.add(Button.primary("NEXT_PAGE", "⏩"));
        return result;
    }

    private void createNewMessage() {
        queueMessage = guildMusicManager.getData().getOrCreateMainChannel(guildMusicManager.getGuild())
                .sendMessage(getMessage(guildMusicManager.getQueue())).complete();
    }

    public void getNextPage() {
        page++;
        update();
    }

    public void getPreviousPage() {
        page--;
        update();
    }

    @Override
    public void update() {
        List<AudioTrack> queue = guildMusicManager.getQueue();
        if (0 > MAX_SONGS_NUMBER * (page - 1) || MAX_SONGS_NUMBER * (page - 1) > queue.size()) {
            page = queue.size() / MAX_SONGS_NUMBER + 1;
        }
        try {
            queueMessage.editMessage(getMessage(queue)).complete();
        } catch (Exception ignored) {
            createNewMessage();
        }
    }

    private Message getMessage(List<AudioTrack> queue) {
        return new MessageBuilder().append("__Queue__:\n")
                .append(
                        IntStream.range(MAX_SONGS_NUMBER * (page - 1), Math.min(MAX_SONGS_NUMBER * page, queue.size()))
                                .mapToObj(
                                        i -> String.format(
                                                "%d) `%s` *by* ***%s*** [`%s`]",
                                                i + 1,
                                                queue.get(i).getInfo().title,
                                                queue.get(i).getUserData(User.class).getName(),
                                                Utils.normalizeTime(queue.get(i).getDuration())
                                        )
                                ).collect(Collectors.joining("\n"))
                ).append(
                        String.format(
                                "\nPage %d / %d | Total queue duration: `%s`",
                                page,
                                queue.size() / MAX_SONGS_NUMBER + 1,
                                Utils.normalizeTime(
                                        queue.stream().map(AudioTrack::getDuration).reduce(0L, Long::sum)
                                )
                        )
                )
                .setActionRows(ActionRow.of(getButtons())).build();
    }
}
