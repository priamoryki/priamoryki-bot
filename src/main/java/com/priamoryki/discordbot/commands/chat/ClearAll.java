package com.priamoryki.discordbot.commands.chat;

import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.utils.DataSource;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageHistory;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class ClearAll implements Command {
    private final DataSource data;

    public ClearAll(DataSource data) {
        this.data = data;
    }

    @Override
    public List<String> getNames() {
        return List.of("clear_all");
    }

    @Override
    public String getDescription() {
        return "Clears all messages in channel except pinned";
    }

    @Override
    public boolean isAvailableFromChat() {
        return true;
    }

    @Override
    public void execute(Guild guild, Member member, List<String> args) {
        MessageHistory.getHistoryFromBeginning(data.getOrCreateMainChannel(guild)).complete().getRetrievedHistory()
                .stream().filter(m -> !m.isPinned()).forEach(m -> m.delete().queue());
    }
}
