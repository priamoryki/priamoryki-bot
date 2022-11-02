package com.priamoryki.discordbot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public interface Command extends Comparable<Command> {
    List<String> getNames();

    default String getDescription() {
        return "Description is not provided";
    }

    default List<OptionData> getOptions() {
        return List.of();
    }

    default boolean isAvailableFromChat() {
        return false;
    }

    void execute(Guild guild, Member member, List<String> args);

    @Override
    default int compareTo(@NotNull Command command) {
        return getNames().get(0).compareTo(command.getNames().get(0));
    }
}
