package com.priamoryki.discordbot.common.auth.service;

import com.priamoryki.discordbot.api.common.BotData;
import com.priamoryki.discordbot.api.common.ExceptionNotifier;
import com.priamoryki.discordbot.api.database.entities.YoutubeAuth;
import com.priamoryki.discordbot.api.database.repositories.YoutubeAuthRepository;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.http.YoutubeOauth2Handler;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * @author Pavel Lymar
 */
@Service
public class YoutubeAuthService implements CommandLineRunner {
    private static final String AUTHORIZATION_PENDING = "authorization_pending";
    private static final String SLOW_DOWN = "slow_down";
    private static final String ACCESS_DENIED = "access_denied";
    private static final String EXPIRED_TOKEN = "expired_token";
    private static final long MIN_POLL_INTERVAL_SECONDS = 5;
    private static final long SLOW_DOWN_STEP_SECONDS = 5;
    private static final long DEFAULT_DEVICE_CODE_LIFETIME_SECONDS = 1800;

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final YoutubeAudioSourceManager youtubeAudioSourceManager;
    private final YoutubeAuthRepository youtubeAuthRepository;
    private final BotData data;
    private final ExceptionNotifier exceptionNotifier;

    public YoutubeAuthService(
            YoutubeAudioSourceManager youtubeAudioSourceManager,
            YoutubeAuthRepository youtubeAuthRepository,
            BotData data,
            ExceptionNotifier exceptionNotifier
    ) {
        this.youtubeAudioSourceManager = youtubeAudioSourceManager;
        this.youtubeAuthRepository = youtubeAuthRepository;
        this.data = data;
        this.exceptionNotifier = exceptionNotifier;
    }

    @Override
    public void run(String... args) {
        String storedRefreshToken = getStoredRefreshToken();
        if (!storedRefreshToken.isBlank()) {
            youtubeAudioSourceManager.useOauth2(storedRefreshToken, true);
            logger.info("YouTube OAuth enabled using stored refresh token");
            return;
        }

        Thread.ofVirtual().name("youtube-oauth").start(this::authorize);
    }

    private void authorize() {
        try {
            YoutubeOauth2Handler oauth2Handler = youtubeAudioSourceManager.getOauth2Handler();
            JsonBrowser deviceCodeResponse = oauth2Handler.fetchDeviceCode();
            String deviceCode = deviceCodeResponse.get("device_code").text();
            String userCode = deviceCodeResponse.get("user_code").text();
            String verificationUrl = deviceCodeResponse.get("verification_url").text();
            if (deviceCode == null || userCode == null || verificationUrl == null) {
                logger.error("Unexpected device code response: {}", deviceCodeResponse.format());
                return;
            }

            long intervalSeconds = Math.max(
                    deviceCodeResponse.get("interval").asLong(MIN_POLL_INTERVAL_SECONDS),
                    MIN_POLL_INTERVAL_SECONDS
            );
            Instant deadline = Instant.now().plusSeconds(
                    deviceCodeResponse.get("expires_in").asLong(DEFAULT_DEVICE_CODE_LIFETIME_SECONDS)
            );

            logger.info("Waiting for YouTube authorization on {} with code {}", verificationUrl, userCode);
            notifyAuthor("Open %s and enter code `%s` to authorize YouTube playback.".formatted(verificationUrl, userCode));

            String refreshToken = pollRefreshToken(oauth2Handler, deviceCode, intervalSeconds, deadline);
            if (refreshToken == null) {
                return;
            }

            youtubeAudioSourceManager.useOauth2(refreshToken, true);
            saveRefreshToken(refreshToken);
            logger.info("YouTube OAuth enabled using new refresh token");
            notifyAuthor("YouTube authorization completed.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("YouTube authorization was interrupted", e);
        } catch (Exception e) {
            logger.error("YouTube authorization failed", e);
            exceptionNotifier.notify(e);
        }
    }

    private String pollRefreshToken(
            YoutubeOauth2Handler oauth2Handler,
            String deviceCode,
            long intervalSeconds,
            Instant deadline
    ) throws IOException, InterruptedException {
        long currentIntervalSeconds = intervalSeconds;
        while (Instant.now().isBefore(deadline)) {
            Thread.sleep(Duration.ofSeconds(currentIntervalSeconds));

            JsonBrowser response = oauth2Handler.fetchRefreshToken(deviceCode);
            String error = response.get("error").text();
            if (error == null) {
                String refreshToken = response.get("refresh_token").text();
                if (refreshToken == null) {
                    logger.error("Unexpected token response: {}", response.format());
                }
                return refreshToken;
            }

            switch (error) {
                case AUTHORIZATION_PENDING -> {
                }
                case SLOW_DOWN -> currentIntervalSeconds += SLOW_DOWN_STEP_SECONDS;
                case ACCESS_DENIED -> {
                    notifyAuthor("YouTube authorization was denied.");
                    return null;
                }
                case EXPIRED_TOKEN -> {
                    notifyAuthor("YouTube authorization code expired. Restart the bot to get a new one.");
                    return null;
                }
                default -> {
                    logger.error("Unexpected OAuth error: {}", error);
                    return null;
                }
            }
        }

        notifyAuthor("YouTube authorization code expired. Restart the bot to get a new one.");
        return null;
    }

    private String getStoredRefreshToken() {
        return youtubeAuthRepository.findById(YoutubeAuth.SINGLETON_ID)
                .map(YoutubeAuth::getRefreshToken)
                .orElse("");
    }

    private void saveRefreshToken(String refreshToken) {
        YoutubeAuth youtubeAuth = youtubeAuthRepository.findById(YoutubeAuth.SINGLETON_ID)
                .orElseGet(YoutubeAuth::new);
        youtubeAuth.setId(YoutubeAuth.SINGLETON_ID);
        youtubeAuth.setRefreshToken(refreshToken);
        youtubeAuthRepository.save(youtubeAuth);
    }

    private void notifyAuthor(String message) throws InterruptedException {
        data.awaitReady();
        data.retrieveBotAuthor()
                .flatMap(User::openPrivateChannel)
                .flatMap(channel -> channel.sendMessage(message))
                .queue(null, error -> logger.error("Can't send YouTube authorization message", error));
    }
}
