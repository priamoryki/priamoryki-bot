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
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.SeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.InternalAudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.LocalAudioTrackExecutor;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class TikTokAudioTrack extends MpegTrack {
    private Pair<String, String> urlCache = null;
    private boolean failedOnce = false;

    public TikTokAudioTrack(AudioTrackInfo trackInfo, AbstractDuncteBotHttpSource manager) {
        super(trackInfo, manager);
    }

    @Override
    public String getPlaybackUrl() {
        try {
            if (urlCache == null) {
                urlCache = loadPlaybackUrl();
            }

            return getUrl();
        } catch (Exception e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Could not load TikTok video", SUSPICIOUS, e);
        }
    }

    @Override
    public void process(LocalAudioTrackExecutor executor) throws Exception {
        try (HttpInterface httpInterface = getHttpInterface()) {
            loadStream(executor, httpInterface);
        }
    }

    @Override
    protected void loadStream(LocalAudioTrackExecutor localExecutor, HttpInterface httpInterface) throws Exception {
        try {
            super.loadStream(localExecutor, httpInterface);
        } catch (Exception e) {
            if (failedOnce) {
                throw e;
            }

            failedOnce = true;
            loadStream(localExecutor, httpInterface);
        }
    }

    private Pair<String, String> loadPlaybackUrl() {
        TikTokAudioSourceManager.MetaData metaData = getSourceManager().extractData(
                trackInfo.author,
                trackInfo.identifier
        );

        return Pair.of(metaData.videoUrl, metaData.musicUrl);
    }

    @Override
    protected InternalAudioTrack createAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream stream) {
        String url = getUrl();
        if (failedOnce || url.contains(".mp3")) {
            return new Mp3AudioTrack(trackInfo, stream);
        }

        return super.createAudioTrack(trackInfo, stream);
    }

    @Override
    protected HttpInterface getHttpInterface() {
        return getSourceManager().getHttpInterface();
    }

    @Override
    public TikTokAudioSourceManager getSourceManager() {
        return (TikTokAudioSourceManager) super.getSourceManager();
    }

    @Override
    protected AudioTrack makeShallowClone() {
        return new TikTokAudioTrack(trackInfo, getSourceManager());
    }

    private String getUrl() {
        if (urlCache.getLeft().isEmpty()) {
            return urlCache.getRight();
        }

        if (urlCache.getRight().isEmpty()) {
            return urlCache.getLeft();
        }

        return failedOnce ? urlCache.getRight() : urlCache.getLeft();
    }
}
