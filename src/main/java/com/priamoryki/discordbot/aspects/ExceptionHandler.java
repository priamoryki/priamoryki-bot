package com.priamoryki.discordbot.aspects;

import com.priamoryki.discordbot.api.common.ExceptionNotifier;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * @author Pavel Lymar
 */
@Aspect
@Component
public class ExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExceptionNotifier exceptionNotifier;

    public ExceptionHandler(ExceptionNotifier exceptionNotifier) {
        this.exceptionNotifier = exceptionNotifier;
    }

    @PostConstruct
    public void registerExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            logger.error("Uncaught exception in thread {}: {}", thread.getName(), e.getMessage(), e);
            exceptionNotifier.notify(e);
        });
    }

    @AfterThrowing(pointcut = "execution(* com.priamoryki.discordbot.api.events.EventsListener..*(..))", throwing = "e")
    public void interceptAllProjectExceptions(Exception e) {
        logger.error("Uncaught exception: {}", e.getMessage(), e);
        exceptionNotifier.notify(e);
    }
}
