package com.priamoryki.discordbot.commands;

import com.priamoryki.discordbot.api.common.ExceptionNotifier;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * @author Pavel Lymar
 */
@Service
public class CommandsStorage {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExceptionNotifier exceptionNotifier;
    private final HashMap<String, Command> commands;

    public CommandsStorage(ExceptionNotifier exceptionNotifier) {
        this.exceptionNotifier = exceptionNotifier;
        commands = new HashMap<>();
    }

    public CommandsStorage(ExceptionNotifier exceptionNotifier, Command... commands) {
        this(exceptionNotifier);
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

    public void executeCommand(String name, Guild guild, Member member) {
        executeCommand(name, guild, member, Collections.emptyList());
    }

    public void executeCommand(String name, Guild guild, Member member, List<String> args) {
        try {
            Command command = getCommand(name);
            command.execute(guild, member, args);
        } catch (CommandException e) {
            logger.debug(e.getMessage());
        } catch (Exception e) {
            logger.error("Error on command execution", e);
            exceptionNotifier.notify(e);
        }
    }

    public void executeCommandWithPermissions(
            String name,
            Guild guild,
            Member member,
            Runnable onSuccessfulExecution,
            Consumer<Exception> onCommandException,
            Consumer<Exception> onException,
            boolean checkAvailability
    ) {
        executeCommandWithPermissions(
                name,
                guild,
                member,
                Collections.emptyList(),
                onSuccessfulExecution,
                onCommandException,
                onException,
                checkAvailability
        );
    }

    public void executeCommandWithPermissions(
            String name,
            Guild guild,
            Member member,
            List<String> args,
            Runnable onSuccessfulExecution,
            Consumer<Exception> onCommandException,
            Consumer<Exception> onException,
            boolean checkAvailability
    ) {
        try {
            Command command = getCommand(name);
            if (command == null || checkAvailability && !command.isAvailableFromChat()) {
                throw new CommandException("Can't find such command!");
            }
            command.executeWithPermissions(guild, member, args);
            onSuccessfulExecution.run();
        } catch (CommandException e) {
            logger.debug(e.getMessage());
            onCommandException.accept(e);
        } catch (Exception e) {
            logger.error("Error on command execution", e);
            onException.accept(e);
            exceptionNotifier.notify(e);
        }
    }
}
