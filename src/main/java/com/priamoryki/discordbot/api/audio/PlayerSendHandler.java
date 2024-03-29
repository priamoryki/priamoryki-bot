package com.priamoryki.discordbot.api.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

/**
 * @author Pavel Lymar
 */
public class PlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer player;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;

    public PlayerSendHandler(AudioPlayer player) {
        this.player = player;
        this.buffer = ByteBuffer.allocate(1024);
        this.frame = new MutableAudioFrame();
        frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        return player.provide(frame);
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return buffer.flip();
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
