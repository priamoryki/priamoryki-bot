package com.priamoryki.discordbot.utils.auth.holder;

import java.time.Instant;

public record TokenHolder(
        String accessToken,
        Instant accessTokenExpirationTime,
        String refreshToken,
        Instant refreshTokenExpirationTime
) {
    public static final TokenHolder EMPTY = new TokenHolder("", Instant.EPOCH, "", Instant.EPOCH);
}
