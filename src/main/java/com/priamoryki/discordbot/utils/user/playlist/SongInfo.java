package com.priamoryki.discordbot.utils.user.playlist;

/**
 * @author Pavel Lymar
 */
public class SongInfo {
    private final String name;
    private final String url;
    private final long length;

    public SongInfo(String name, String url, long length) {
        this.name = name;
        this.url = url;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public long getLength() {
        return length;
    }
}
