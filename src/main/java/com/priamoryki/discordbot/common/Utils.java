package com.priamoryki.discordbot.common;

import com.priamoryki.discordbot.api.audio.customsources.CustomUserData;
import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.commands.CommandException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.apache.http.HttpRequest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Pavel Lymar
 */
public final class Utils {
    private Utils() {

    }

    public static boolean isUrl(String url) {
        try {
            URI.create(url).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String firstNotEmpty(String... strings) {
        return Arrays.stream(strings).filter(s -> !s.isEmpty()).findFirst().orElse("");
    }

    public static <T> List<T> getReversedList(List<T> list) {
        List<T> result = new ArrayList<>(list);
        Collections.reverse(result);
        return result;
    }

    public static String normalizeTime(long time) {
        time /= 1000;
        List<Integer> dividers = List.of(60, 60, Integer.MAX_VALUE);
        int minDividers = 2;

        long divider = 1;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < dividers.size(); i++) {
            if (i >= minDividers && time / divider == 0) {
                break;
            }
            var current = dividers.get(i);
            if (!builder.isEmpty()) {
                builder.insert(0, ":");
            }
            builder.insert(0, String.format("%02d", (time / divider) % current));
            divider *= current;
        }
        return builder.toString();
    }

    public static long parseTime(String s) {
        List<String> list = getReversedList(Arrays.stream(s.split(":")).toList());
        long time = 0;
        for (int i = 0; i < Math.min(3, list.size()); i++) {
            time += Math.pow(60, i) * Long.parseLong(list.get(i));
        }
        return time;
    }

    public static void validateId(int id, int size) throws CommandException {
        if (1 > id) {
            throw new CommandException("Id parameter should be natural number!");
        }
        if (id > size) {
            throw new CommandException("Id parameter shouldn't be more than size!");
        }
    }

    public static void validateBounds(int from, int to, int size, String message) throws CommandException {
        if (from > to) {
            throw new CommandException("Incorrect order of arguments!");
        }
        if (1 > from) {
            throw new CommandException(message);
        }
        if (from > size) {
            throw new CommandException(message);
        }
        if (to > size) {
            throw new CommandException(message);
        }
    }

    public static void fakeChrome(HttpRequest request) {
        fakeChrome(request, false);
    }

    public static void fakeChrome(HttpRequest request, boolean isVideo) {
        getFakeChromeHeaders(isVideo).forEach(request::setHeader);
    }

    public static Map<String, String> getFakeChromeHeaders(boolean isVideo) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "keep-alive");
        headers.put("DNT", "1");
        headers.put("Upgrade-Insecure-Requests", "1");
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,video/mp4,image/avif,image/webp,*/*;q=0.8");
        headers.put("Accept-Encoding", "none");
        headers.put("TE", "trailers");
        headers.put("Accept-Language", "en-US,en;q=0.9");
        headers.put("Sec-Fetch-Dest", isVideo ? "empty" : "document");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", "same-site");
        headers.put("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/114.0");
        return headers;
    }

    public static boolean isTheSameVoiceChannelWithMember(Member bot, Member member) {
        GuildVoiceState memberVoiceState = member.getVoiceState();
        GuildVoiceState botVoiceState = bot.getVoiceState();
        return botVoiceState == null || botVoiceState.getChannel() == null ||
                memberVoiceState != null && memberVoiceState.getChannel() == botVoiceState.getChannel();
    }

    public static SlashCommandData commandToSlashCommand(String name, Command command) {
        return Commands.slash(name, command.getDescription())
                .addOptions(command.getOptions())
                .setContexts(InteractionContextType.GUILD);
    }

    public static String audioTrackToString(AudioTrack track) {
        CustomUserData userData = track.getUserData(CustomUserData.class);
        User skippedBy = userData.getSkippedBy();
        return String.format(
                "`%s` *by* ***%s*** [`%s`] ([link](<%s>))%s",
                track.getInfo().title,
                userData.getQueuedBy().getName(),
                Utils.normalizeTime(track.getDuration()),
                track.getInfo().uri,
                skippedBy != null ? String.format(" *skipped by %s*", skippedBy.getName()) : ""
        );
    }
}
