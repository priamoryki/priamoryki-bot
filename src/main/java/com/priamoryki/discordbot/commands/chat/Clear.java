package com.priamoryki.discordbot.commands.chat;

import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.utils.DataSource;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;

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
    public void execute(Guild guild, Member member, List<String> args) {
        if (args.size() != 1) {
            return;
        }
        long id = Long.parseLong(args.get(0));
        List<Message> messages = MessageHistory.getHistoryFromBeginning(data.getOrCreateMainChannel(guild))
                .complete()
                .getRetrievedHistory();
        for (Message msg : messages) {
            if (msg.getIdLong() == id && !data.isBot(member.getUser())) {
                msg.delete().queue();
            }
        }
    }
}
