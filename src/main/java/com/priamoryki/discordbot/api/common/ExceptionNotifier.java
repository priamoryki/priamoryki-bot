package com.priamoryki.discordbot.api.common;

import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * @author Pavel Lymar
 */
@Service
public class ExceptionNotifier {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BotData data;

    public ExceptionNotifier(BotData data) {
        this.data = data;
    }

    public void notify(Throwable e) {
        try {
            File file = File.createTempFile("stacktrace_", ".log");

            file.deleteOnExit();
            try (FileWriter fw = new FileWriter(file); PrintWriter pw = new PrintWriter(fw)) {
                e.printStackTrace(pw);
            }

            data.getBotAuthor().openPrivateChannel().queue(
                    channel -> channel.sendMessage("Exception occurred!").addFiles(FileUpload.fromData(file)).queue()
            );
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
}
