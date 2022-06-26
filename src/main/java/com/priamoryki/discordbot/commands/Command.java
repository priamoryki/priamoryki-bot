package com.priamoryki.discordbot.commands;

import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public interface Command {
    List<String> getNames();

    boolean isAvailableFromChat();

    void execute(Message message, List<String> args);
}
