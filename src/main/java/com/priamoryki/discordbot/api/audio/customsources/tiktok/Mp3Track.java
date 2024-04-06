/*
 * Copyright 2021 Duncan "duncte123" Sterken
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.priamoryki.discordbot.api.audio.customsources.tiktok;

import com.priamoryki.discordbot.api.audio.customsources.AbstractDuncteBotHttpSource;
import com.sedmelluq.discord.lavaplayer.container.mp3.Mp3AudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.PersistentHttpStream;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.DelegatedAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class Mp3Track extends DelegatedAudioTrack {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AbstractDuncteBotHttpSource manager;

    public Mp3Track(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager) {
        super(trackInfo);
        this.manager = manager;
    }

    protected HttpInterface getHttpInterface() {
        return manager.getHttpInterface();
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        try (HttpInterface httpInterface = getHttpInterface()) {
            loadStream(executor, httpInterface);
        }
    }

    protected void loadStream(LocalAudioTrackExecutor localExecutor, HttpInterface httpInterface) throws Exception {
        String trackUrl = getPlaybackUrl();
        logger.debug("Starting {} track from URL: {}", manager.getSourceName(), trackUrl);
        // Setting contentLength (last param) to null makes it default to Long.MAX_VALUE
        try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(trackUrl), getTrackDuration())) {
            processDelegate(createAudioTrack(trackInfo, stream), localExecutor);
        }
    }

    protected InternalAudioTrack createAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream stream) {
        return new Mp3AudioTrack(trackInfo, stream);
    }

    /**
     * A special helper to determine the length of the file in milliseconds.
     *
     * @return The clip length in milliseconds, for some sources this needs to be set to unknown for them to properly work.
     */
    protected long getTrackDuration() {
        return trackInfo.length;
    }

    public String getPlaybackUrl() {
        return trackInfo.identifier;
    }

    @Override
    public AbstractDuncteBotHttpSource getSourceManager() {
        return manager;
    }
}
