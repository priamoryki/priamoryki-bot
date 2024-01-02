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
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.priamoryki.discordbot.utils.Utils.fakeChrome;
import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class TikTokAudioSourceManager extends AbstractDuncteBotHttpSource {
    protected static final Pattern VIDEO_REGEX =
            Pattern.compile("^(https?://)?(www\\.)?tiktok\\.com/@(?<user>[^/]+)/(video|photo)/(?<video>\\d+).*$");
    private static final Pattern JS_REGEX =
            Pattern.compile("<script id=\"SIGI_STATE\" type=\"application/json\">([^<]+)</script>");
    private static final Pattern SIGI_REGEX =
            Pattern.compile("<script id=\"sigi-persisted-data\">\n?window\\[(?:'SIGI_STATE'|\"SIGI_STATE\")](?:\\s+)?=(?:\\s+)?(.*);(?:\\s+)?.*</script>");
    private static final Pattern UNIVERSAL_REGEX =
            Pattern.compile("<script id=\"__UNIVERSAL_DATA_FOR_REHYDRATION__\" type=\"application/json\">([^<]+)</script>");
    private static final Map<Pattern, MetaDataGetter> patterns = Map.of(
            JS_REGEX, new JsMetaDataGetter(),
            SIGI_REGEX, new SigiMetaDataGetter(),
            UNIVERSAL_REGEX, new UniversalMetaDataGetter()
    );
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final TikTokAudioTrackHttpManager httpManager = new TikTokAudioTrackHttpManager();

    public TikTokAudioSourceManager() {
        super(false);
    }

    private static MetaData getMetaData(String url, JsonBrowser base) {
        MetaData metaData = new MetaData();
        JsonBrowser videoJson = base.get("video");
        JsonBrowser musicJson = base.get("music");

        metaData.pageUrl = url;
        metaData.videoId = base.get("id").safeText();
        metaData.videoUrl = videoJson.get("downloadAddr").text();
        metaData.cover = videoJson.get("cover").safeText();
        metaData.title = base.get("desc").safeText();

        metaData.uri = videoJson.get("playAddr").safeText();
        metaData.duration = Integer.parseInt(videoJson.get("duration").safeText());
        if (metaData.duration == 0) {
            metaData.duration = Integer.parseInt(musicJson.get("duration").safeText());
        }

        metaData.musicUrl = musicJson.get("playUrl").text();

        metaData.uniqueId = base.get("author").get("uniqueId").safeText();

        return metaData;
    }

    @Override
    public String getSourceName() {
        return "tiktok";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        Matcher matcher = VIDEO_REGEX.matcher(reference.identifier);

        if (!matcher.matches()) {
            return null;
        }

        String user = matcher.group("user");
        String video = matcher.group("video");

        try {
            MetaData metaData = extractData(user, video);

            return new TikTokAudioTrack(metaData.toTrackInfo(), this);
        } catch (Exception e) {
            throw ExceptionTools.wrapUnfriendlyExceptions("Something went wrong", SUSPICIOUS, e);
        }
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return true;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        // Nothing to encode
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return new TikTokAudioTrack(trackInfo, this);
    }

    protected MetaData extractData(String userId, String videoId) {
        logger.info("userId: {}, videoId: {}", userId, videoId);
        String url = String.format("https://www.tiktok.com/@%s/video/%s", userId, videoId);
        return extractData(url);
    }

    @Override
    public HttpInterface getHttpInterface() {
        return httpManager.getHttpInterface();
    }

    private MetaData extractData(String url) {
        HttpGet httpGet = new HttpGet(url);

        fakeChrome(httpGet);

        try (CloseableHttpResponse response = getHttpInterface().execute(httpGet)) {
            String html = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            for (var entry : patterns.entrySet()) {
                var pattern = entry.getKey();
                var function = entry.getValue();
                Matcher matcher = pattern.matcher(html);
                if (matcher.find()) {
                    return function.getMetaData(url, matcher.group(1).trim());
                }
            }
        } catch (Exception e) {
            logger.error("Failed to extract tiktok video", e);
        }
        throw ExceptionTools.wrapUnfriendlyExceptions("Failed to extract tiktok video", SUSPICIOUS, null);
    }

    private interface MetaDataGetter {
        MetaData getMetaData(String url, String code) throws IOException;
    }

    protected static class MetaData {
        // video
        String cover; // image url
        String pageUrl;
        String videoId;
        String videoUrl;
        String uri;
        int duration; // in seconds
        String title;

        // backup
        String musicUrl;

        // author
        String uniqueId;

        AudioTrackInfo toTrackInfo() {
            return new AudioTrackInfo(
                    title,
                    uniqueId,
                    duration * 1000L,
                    videoId,
                    false,
                    pageUrl,
                    cover,
                    null
            );
        }
    }

    private static class JsMetaDataGetter implements MetaDataGetter {
        @Override
        public MetaData getMetaData(String url, String code) throws IOException {
            JsonBrowser json = JsonBrowser.parse(code);
            String videoId = json.get("ItemList").get("video").get("list").index(0).text();
            JsonBrowser video = json.get("ItemModule").get(videoId);

            return TikTokAudioSourceManager.getMetaData(url, video);
        }
    }

    private static class SigiMetaDataGetter implements MetaDataGetter {
        @Override
        public MetaData getMetaData(String url, String code) throws IOException {
            JsonBrowser json = JsonBrowser.parse(code);
            String videoId = json.get("ItemList").get("video").get("keyword").text();
            JsonBrowser video = json.get("ItemModule").get(videoId);

            return TikTokAudioSourceManager.getMetaData(url, video);
        }
    }

    private static class UniversalMetaDataGetter implements MetaDataGetter {
        @Override
        public MetaData getMetaData(String url, String code) throws IOException {
            JsonBrowser json = JsonBrowser.parse(code);
            JsonBrowser video = json.get("__DEFAULT_SCOPE__").get("webapp.video-detail").get("itemInfo").get("itemStruct");

            return TikTokAudioSourceManager.getMetaData(url, video);
        }
    }
}
