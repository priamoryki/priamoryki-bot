package com.priamoryki.discordbot.utils.messages;

import com.priamoryki.discordbot.api.audio.GuildMusicManager;
import com.priamoryki.discordbot.utils.Utils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

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
                .sendMessage(fillBuilder(new MessageCreateBuilder(), guildMusicManager.getQueue()).build()).complete();
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
        int lastSongNumber = MAX_SONGS_NUMBER * (page - 1);
        if (0 > lastSongNumber) {
            page = 1;
        }
        if (lastSongNumber > queue.size()) {
            page = queue.size() / MAX_SONGS_NUMBER + 1;
        }
        try {
            queueMessage.editMessage(fillBuilder(new MessageEditBuilder(), queue).build()).complete();
        } catch (Exception ignored) {
            createNewMessage();
        }
    }

    private <T, R extends AbstractMessageBuilder<T, R>> AbstractMessageBuilder<T, R> fillBuilder(AbstractMessageBuilder<T, R> messageBuilder, List<AudioTrack> queue) {
        String content = "__Queue__:\n" +
                IntStream.range(MAX_SONGS_NUMBER * (page - 1), Math.min(MAX_SONGS_NUMBER * page, queue.size()))
                        .mapToObj(
                                i -> String.format(
                                        "%d) `%s` *by* ***%s*** [`%s`]",
                                        i + 1,
                                        queue.get(i).getInfo().title,
                                        queue.get(i).getUserData(User.class).getName(),
                                        Utils.normalizeTime(queue.get(i).getDuration())
                                )
                        ).collect(Collectors.joining("\n")) +
                String.format(
                        "\nPage %d / %d | Total queue duration: `%s`",
                        page,
                        queue.size() / MAX_SONGS_NUMBER + 1,
                        Utils.normalizeTime(
                                queue.stream().map(AudioTrack::getDuration).reduce(0L, Long::sum)
                        )
                );
        return messageBuilder.setContent(content).setComponents(ActionRow.of(getButtons()));
    }
}
