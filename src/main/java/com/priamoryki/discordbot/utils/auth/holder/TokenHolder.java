package com.priamoryki.discordbot.utils.auth.holder;

import java.time.Instant;

/**
 * @author Pavel Lymar
 */
public record TokenHolder(
        String accessToken,
        Instant accessTokenExpirationTime,
        String refreshToken,
        Instant refreshTokenExpirationTime
) {
    public static final TokenHolder EMPTY = new TokenHolder("", Instant.EPOCH, "", Instant.EPOCH);
}
