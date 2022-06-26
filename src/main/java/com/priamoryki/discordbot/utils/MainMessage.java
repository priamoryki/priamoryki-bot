package com.priamoryki.discordbot.utils;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Lymar
 */
public class MainMessage {
    public static List<Button> getButtons() {
        List<Button> result = new ArrayList<>();
        result.add(Button.primary("CLEAR_ALL", "🧹"));
        return result;
    }

    public static Message getDefaultMessage(String text) {
        // TODO Avoid `text`
        return new MessageBuilder().setContent(
                String.format("```%s```", text)
        ).setActionRows(ActionRow.of(getButtons())).build();
    }
}
