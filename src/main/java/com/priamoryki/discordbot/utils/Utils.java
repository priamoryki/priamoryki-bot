package com.priamoryki.discordbot.utils;

import com.priamoryki.discordbot.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Pavel Lymar
 */
public class Utils {
    public static boolean isUrl(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public static String normalizeTime(long time) {
        time /= 1000;
        long s = time % 60;
        long m = (time / 60) % 60;
        long h = (time / (60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public static SlashCommandData commandToSlashCommand(String name, Command command) {
        return Commands.slash(name, command.getDescription())
                .addOptions(command.getOptions())
                .setGuildOnly(true);
    }
}
