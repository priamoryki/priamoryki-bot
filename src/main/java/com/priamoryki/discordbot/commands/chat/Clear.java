package com.priamoryki.discordbot.commands.chat;

import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.utils.DataSource;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class Clear implements Command {
    private final DataSource data;

    public Clear(DataSource data) {
        this.data = data;
    }

    @Override
    public List<String> getNames() {
        return List.of("clear");
    }

    @Override
    public boolean isAvailableFromChat() {
        return false;
    }

    @Override
    public void execute(Message message, List<String> args) {
        if (message.getChannel().getIdLong() == data.getMainChannelId(message.getGuild().getIdLong())
                && !data.isBotMessage(message)) {
            message.delete().complete();
        }
    }
}
