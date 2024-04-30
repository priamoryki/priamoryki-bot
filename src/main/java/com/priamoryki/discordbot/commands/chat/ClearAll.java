package com.priamoryki.discordbot.commands.chat;

import com.priamoryki.discordbot.api.common.GuildAttributesService;
import com.priamoryki.discordbot.commands.Command;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageHistory;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class ClearAll implements Command {
    private final GuildAttributesService guildAttributesService;

    public ClearAll(GuildAttributesService guildAttributesService) {
        this.guildAttributesService = guildAttributesService;
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
    public void execute(Guild guild, Member member, List<String> args) {
        MessageHistory
                .getHistoryFromBeginning(guildAttributesService.getOrCreateMainChannel(guild))
                .complete()
                .getRetrievedHistory()
                .stream().filter(m -> !m.isPinned()).forEach(m -> m.delete().queue());
    }
}
