package com.priamoryki.discordbot.api.messages;

import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.commands.CommandsStorage;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Pavel Lymar
 */
public class MainMessage {
    private static String text;

    private MainMessage() {

    }

    public static List<Button> getButtons() {
        return List.of(
                Button.primary("CLEAR_ALL", Emoji.fromUnicode("🧹"))
        );
    }

    public static <T, R extends AbstractMessageBuilder<T, R>> AbstractMessageBuilder<T, R> fillWithDefaultMessage(
            AbstractMessageBuilder<T, R> messageBuilder
    ) {
        return messageBuilder.setContent(
                String.format("```%s```", text)
        ).setComponents(ActionRow.of(getButtons()));
    }

    public static void fillTextField(CommandsStorage storage) {
        Map<String, List<Command>> packageToCommands = new HashMap<>();
        for (Command command : storage.getCommands()) {
            if (command.isAvailableFromChat()) {
                String packageName = command.getClass().getPackageName();
                packageToCommands.computeIfAbsent(packageName, k -> new ArrayList<>()).add(command);
            }
        }

        StringBuilder result = new StringBuilder();
        for (var entry : packageToCommands.entrySet()) {
            String packageName = entry.getKey();
            String beautifulPackageName = packageName.substring(packageName.lastIndexOf('.') + 1).toUpperCase();
            result.append(beautifulPackageName).append(":").append("\n");
            for (Command command : entry.getValue()) {
                String name = command.getNames().get(0);
                String options = command.getOptions().stream().map(OptionData::getName).collect(Collectors.joining(" "));
                result.append("\t").append(name).append(" ").append(options).append("\n");
            }
            result.append("\n");
        }
        result.append("INFORM ABOUT ISSUE:").append("\n").append("github.com/priamoryki/priamoryki-bot/issues");

        text = result.toString();
    }
}
