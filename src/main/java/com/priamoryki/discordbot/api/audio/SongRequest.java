package com.priamoryki.discordbot.api.audio;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

/**
 * @author Pavel Lymar
 */
public class SongRequest {
    private final Guild guild;
    private final Member member;
    private final String urlOrName;

    public SongRequest(Guild guild, Member member, String urlOrName) {
        this.guild = guild;
        this.member = member;
        this.urlOrName = urlOrName;
    }

    public Guild getGuild() {
        return guild;
    }

    public Member getMember() {
        return member;
    }

    public String getUrlOrName() {
        return urlOrName;
    }
}
