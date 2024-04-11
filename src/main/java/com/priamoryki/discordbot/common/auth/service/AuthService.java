package com.priamoryki.discordbot.common.auth.service;

import com.priamoryki.discordbot.common.auth.holder.TokenHolder;

/**
 * @author Pavel Lymar
 */
public interface AuthService {
    TokenHolder auth();

    TokenHolder refresh(String refreshToken);
}
