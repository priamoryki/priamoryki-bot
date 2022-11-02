package com.priamoryki.discordbot.utils;

import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.AbstractMessageBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Lymar
 */
public class MainMessage {
    private static final String mainMessagePath = "data/MainMessage.txt";
    private static String text;

    public static List<Button> getButtons() {
        List<Button> result = new ArrayList<>();
        result.add(Button.primary("CLEAR_ALL", "🧹"));
        return result;
    }

    private static void parseMainMessage() {
        if (text == null) {
            try {
                text = new String(Files.readAllBytes(Paths.get(mainMessagePath)));
            } catch (IOException ignored) {
            }
        }
    }

    public static <T, R extends AbstractMessageBuilder<T, R>> AbstractMessageBuilder<T, R> fillWithDefaultMessage(AbstractMessageBuilder<T, R> messageBuilder) {
        parseMainMessage();
        return messageBuilder.setContent(
                String.format("```%s```", text)
        ).setComponents(ActionRow.of(getButtons()));
    }
}
