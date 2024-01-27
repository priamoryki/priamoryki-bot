package com.priamoryki.discordbot.commands.chat;

import com.priamoryki.discordbot.commands.Command;
import com.priamoryki.discordbot.utils.BotData;
import com.priamoryki.discordbot.utils.GuildAttributesService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;

import java.util.List;

/**
 * @author Pavel Lymar
 */
public class Clear implements Command {
    private final BotData data;
    private final GuildAttributesService guildAttributesService;

    public Clear(BotData data, GuildAttributesService guildAttributesService) {
        this.data = data;
        this.guildAttributesService = guildAttributesService;
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
        Message message = MessageHistory
                .getHistoryFromBeginning(guildAttributesService.getOrCreateMainChannel(guild))
                .complete()
                .getMessageById(id);
        if (message != null && !data.isBot(member.getUser())) {
            message.delete().queue();
        }
    }
}
