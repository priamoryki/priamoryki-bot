package com.priamoryki.discordbot.api.audio;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

/**
 * @author Pavel Lymar
 */
public record SongRequest(Guild guild, Member member, String urlOrName) {
}
