package com.priamoryki.discordbot.common;

import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.commands.CommandException;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.apache.http.HttpRequest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Pavel Lymar
 */
public final class Utils {
    private Utils() {

    }

    public static boolean isUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static String firstNotEmpty(String... strings) {
        return Arrays.stream(strings).filter(s -> !s.isEmpty()).findFirst().orElse("");
    }

    public static String normalizeTime(long time) {
        time /= 1000;
        long s = time % 60;
        long m = (time / 60) % 60;
        long h = (time / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public static long parseTime(String s) {
        List<String> list = Arrays.stream(s.split(":")).collect(Collectors.toList());
        Collections.reverse(list);
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
                .setGuildOnly(true);
    }
}
