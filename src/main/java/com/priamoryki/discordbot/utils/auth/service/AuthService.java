package com.priamoryki.discordbot.utils.auth.service;

import com.priamoryki.discordbot.utils.auth.holder.TokenHolder;

/**
 * @author Pavel Lymar
 */
public interface AuthService {
    TokenHolder auth();

    TokenHolder refresh(String refreshToken);
}
