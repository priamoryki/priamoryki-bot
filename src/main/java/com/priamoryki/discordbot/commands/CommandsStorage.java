package com.priamoryki.discordbot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

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
            if (commands.containsKey(name)) {
                throw new IllegalStateException("Can't put commands with the same name (" + name + ")!");
            }
            commands.putIfAbsent(name, command);
        }
    }

    public void addCommands(Command... commands) {
        Arrays.stream(commands).forEach(this::addCommand);
    }

    public Command getCommand(String name) {
        return commands.get(name);
    }

    public List<Command> getCommands() {
        return new ArrayList<>(new TreeSet<>(commands.values()));
    }

    public void executeCommand(String name, Guild guild, Member member) throws CommandException {
        executeCommand(name, guild, member, Collections.emptyList());
    }

    public void executeCommand(String name, Guild guild, Member member, List<String> args) throws CommandException {
        getCommand(name).execute(guild, member, args);
    }

    public void executeCommandWithPermissions(String name, Guild guild, Member member) throws CommandException {
        executeCommandWithPermissions(name, guild, member, Collections.emptyList());
    }

    public void executeCommandWithPermissions(String name, Guild guild, Member member, List<String> args) throws CommandException {
        getCommand(name).executeWithPermissions(guild, member, args);
    }
}
