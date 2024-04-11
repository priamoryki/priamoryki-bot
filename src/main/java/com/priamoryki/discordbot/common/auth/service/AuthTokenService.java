package com.priamoryki.discordbot.common.auth.service;

import com.priamoryki.discordbot.common.auth.holder.TokenHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.function.Supplier;

/**
 * @author Pavel Lymar
 */
public class AuthTokenService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AuthService authService;
    private final int tokenExpirationOffsetSeconds;
    private TokenHolder tokenHolder = TokenHolder.EMPTY;

    public AuthTokenService(AuthService authService, int tokenExpirationOffsetSeconds) {
        this.authService = authService;
        this.tokenExpirationOffsetSeconds = tokenExpirationOffsetSeconds;
    }

    public String getToken() {
        Instant currentTimeUtc = Instant.now();
        if (isValidToken(currentTimeUtc, tokenHolder.accessTokenExpirationTime())) {
            logger.debug("Getting stored access token");
            return tokenHolder.accessToken();
        }
        synchronized (this) {
            if (isValidToken(currentTimeUtc, tokenHolder.accessTokenExpirationTime())) {
                logger.debug("Getting stored access token");
                return tokenHolder.accessToken();
            }
            if (isValidToken(currentTimeUtc, tokenHolder.refreshTokenExpirationTime())) {
                logger.debug("Updating access token using refresh token");
                tokenHolder = authService.refresh(tokenHolder.refreshToken());
                return tokenHolder.accessToken();
            }
            logger.debug("Updating both tokens");
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

