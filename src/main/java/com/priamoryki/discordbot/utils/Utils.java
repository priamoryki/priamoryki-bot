package com.priamoryki.discordbot.utils;

import com.priamoryki.discordbot.commands.Command;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Pavel Lymar
 */
public final class Utils {
    public static final String UPDATED_PROPERTY = "entity-updated";

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
