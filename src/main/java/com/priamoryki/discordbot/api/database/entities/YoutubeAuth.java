package com.priamoryki.discordbot.api.database.entities;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Date;

/**
 * @author Pavel Lymar
 */
@Entity()
@Table(name = "youtube_auth")
public class YoutubeAuth {
    public static final long SINGLETON_ID = 1L;

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "refresh_token")
    private String refreshToken;

    @UpdateTimestamp
    @Column(name = "last_modified")
    private Date lastModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Date getLastModified() {
        return lastModified;
    }
}
