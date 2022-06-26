package com.priamoryki.discordbot.commands;

import net.dv8tion.jda.api.entities.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Pavel Lymar
 */
public class CommandsStorage {
    private final HashMap<String, Command> commands;

    public CommandsStorage() {
        commands = new HashMap<>();
    }

    public CommandsStorage(Command... commands) {
        this();
        addCommands(commands);
    }

    public void addCommand(Command command) {
        for (String name : command.getNames()) {
            commands.put(name, command);
        }
    }

    public void addCommands(Command... commands) {
        Arrays.stream(commands).forEach(this::addCommand);
    }

    public Command getCommand(String name) {
        return commands.get(name);
    }

    public void executeCommand(String name, Message message) {
        executeCommand(name, message, Collections.emptyList());
    }

    public void executeCommand(String name, Message message, List<String> args) {
        getCommand(name).execute(message, args);
    }
}
