package com.priamoryki.discordbot.api.messages;

import com.priamoryki.discordbot.api.audio.GuildMusicManager;
import com.priamoryki.discordbot.api.audio.customsources.CustomUserData;
import com.priamoryki.discordbot.api.common.GuildAttributesService;
import com.priamoryki.discordbot.common.Utils;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.awt.*;
import java.time.Instant;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.priamoryki.discordbot.common.Utils.firstNotEmpty;

/**
 * @author Pavel Lymar
 */
public class PlayerMessage implements UsefulMessage {
    private static final int BLOCKS_NUMBER = 27;
    private static final long MINIMAL_UPDATE_PERIOD = 15_000;
    private final GuildMusicManager guildMusicManager;
    private final GuildAttributesService guildAttributesService;
    private Message message;
    private Timer timer;
    private long lastUpdateTime;

    public PlayerMessage(GuildMusicManager guildMusicManager, GuildAttributesService guildAttributesService) {
        this.guildMusicManager = guildMusicManager;
        this.guildAttributesService = guildAttributesService;
        createNewMessage();
    }

    private static List<LayoutComponent> getComponents() {
        return List.of(
                ActionRow.of(
                        Button.primary("RESUME", Emoji.fromUnicode("▶")),
                        Button.primary("PAUSE", Emoji.fromUnicode("⏸")),
                        Button.primary("SKIP", Emoji.fromUnicode("⏯")),
                        Button.primary("REPEAT", Emoji.fromUnicode("🔂"))
                ),
                ActionRow.of(
                        Button.primary("BASSBOOST", Emoji.fromUnicode("📢")),
                        Button.primary("NIGHTCORE", Emoji.fromUnicode("🎶")),
                        Button.primary("RESET", Emoji.fromUnicode("🔧"))
                ),
                ActionRow.of(
                        Button.primary("HISTORY", Emoji.fromUnicode("🕰️")),
                        Button.primary("QUEUE_PRINT", Emoji.fromUnicode("🗒️"))
                )
        );
    }

    public static <T, R extends AbstractMessageBuilder<T, R>> AbstractMessageBuilder<T, R> fillWithDefaultMessage(
            AbstractMessageBuilder<T, R> messageBuilder
    ) {
        return messageBuilder.setEmbeds(
                new EmbedBuilder().setColor(Color.BLUE).setTitle("PLAYER MESSAGE").build()
        ).setComponents(getComponents());
    }

    private void createNewMessage() {
        message = guildAttributesService
                .getOrCreatePlayerMessage(guildMusicManager.getGuild());
    }

    @Override
    public void update() {
        // LATER: temp fix for https://github.com/priamoryki/priamoryki-bot/issues/1
        long time = Instant.now().toEpochMilli();
        if (time - lastUpdateTime < MINIMAL_UPDATE_PERIOD) {
            return;
        }
        lastUpdateTime = time;
        try {
            message.editMessage(fillBuilder(new MessageEditBuilder()).build()).complete();
        } catch (Exception ignored) {
            createNewMessage();
        }
    }

    public void startUpdateTask() {
        endUpdateTask();
        long delay = 0;
        long period = Math.max(
                MINIMAL_UPDATE_PERIOD,
                guildMusicManager.getPlayer().getPlayingTrack().getDuration() / BLOCKS_NUMBER
        );
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                update();
            }
        }, delay, period);
    }

    public void endUpdateTask() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
    }

    private <T, R extends AbstractMessageBuilder<T, R>> AbstractMessageBuilder<T, R> fillBuilder(
            AbstractMessageBuilder<T, R> messageBuilder
    ) {
        AudioTrack track = guildMusicManager.getPlayer().getPlayingTrack();
        if (track == null) {
            return fillWithDefaultMessage(messageBuilder);
        }
        User user = track.getUserData(CustomUserData.class).getQueuedBy();
        String title = track.getInfo().title;
        String url = track.getInfo().uri;
        long currentTime = track.getPosition();
        long duration = track.getDuration();
        int blocks = (int) ((track.getPosition() * BLOCKS_NUMBER) / duration);
        boolean isLive = duration == Long.MAX_VALUE;
        if (isLive) {
            blocks = BLOCKS_NUMBER;
        }
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(Color.BLUE).setAuthor("♪" + user.getName() + "♪", null, user.getAvatarUrl());
        if (Utils.isUrl(url)) {
            builder.setTitle(firstNotEmpty(title, url), url);
        } else {
            builder.setTitle(title);
        }
        String timeLine = "🟥".repeat(blocks) + "🟦".repeat(BLOCKS_NUMBER - blocks);
        String timeString = isLive ? "LIVE" : Utils.normalizeTime(currentTime) + " / " + Utils.normalizeTime(duration);
        builder.setDescription(timeLine + "\n" + timeString)
                .setFooter(guildMusicManager.getMusicParameters().getRepeat() ? "On repeat" : "Not on repeat")
                .setTimestamp(Instant.now());
        return messageBuilder.setEmbeds(builder.build()).setComponents(getComponents());
    }
}
