package com.priamoryki.discordbot.entities;

import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Date;

/**
 * @author Pavel Lymar
 */
@Entity()
@Table(name = "servers")
public class ServerInfo {
    @Id
    @PrimaryKeyJoinColumn
    @Column(name = "server_id")
    private Long serverId;

    @Column(name = "channel_id")
    private Long channelId;

    @Column(name = "message_id")
    private Long messageId;

    @UpdateTimestamp
    @Column(name = "last_modified")
    private Date lastModified;

    @Column(name = "player_message_id")
    private Long playerMessageId;

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public Long getPlayerMessageId() {
        return playerMessageId;
    }

    public void setPlayerMessageId(Long playerMessageId) {
        this.playerMessageId = playerMessageId;
    }
}
