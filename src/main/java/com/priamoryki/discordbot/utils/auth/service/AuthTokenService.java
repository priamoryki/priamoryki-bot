package com.priamoryki.discordbot.utils.auth.service;

import com.priamoryki.discordbot.utils.auth.holder.TokenHolder;

import java.time.Instant;
import java.util.function.Supplier;

public class AuthTokenService {
    private final AuthService authService;
    private final int tokenExpirationOffsetSeconds;
    private TokenHolder tokenHolder = TokenHolder.EMPTY;

    public AuthTokenService(AuthService authService, int tokenExpirationOffsetSeconds) {
        this.authService = authService;
        this.tokenExpirationOffsetSeconds = tokenExpirationOffsetSeconds;
    }

    /**
     * Получение токена доступа к системе.
     */
    public String getToken() {
        Instant currentTimeUtc = Instant.now();
        if (isValidToken(currentTimeUtc, tokenHolder.accessTokenExpirationTime())) {
            return tokenHolder.accessToken();
        }
        synchronized (this) {
            if (isValidToken(currentTimeUtc, tokenHolder.accessTokenExpirationTime())) {
                return tokenHolder.accessToken();
            }
            if (isValidToken(currentTimeUtc, tokenHolder.refreshTokenExpirationTime())) {
                tokenHolder = authService.refresh(tokenHolder.refreshToken());
                return tokenHolder.accessToken();
            }
            tokenHolder = authService.auth();
            return tokenHolder.accessToken();
        }
    }

    public <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (Exception e) {
            tokenHolder = authService.auth();
            return action.get();
        }
    }

    private boolean isValidToken(Instant currentTimeUtc, Instant expireTimeUtc) {
        return expireTimeUtc.minusSeconds(tokenExpirationOffsetSeconds).isAfter(currentTimeUtc);
    }
}

