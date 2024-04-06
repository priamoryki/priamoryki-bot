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

import com.sedmelluq.discord.lavaplayer.tools.http.HttpContextFilter;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.stream.Collectors;

import static com.priamoryki.discordbot.utils.Utils.fakeChrome;

public class TikTokAudioTrackHttpManager implements AutoCloseable {
    protected final HttpInterfaceManager httpInterfaceManager;
    private final CookieStore cookieStore = new BasicCookieStore();

    public TikTokAudioTrackHttpManager() {
        httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();

        httpInterfaceManager.configureBuilder(builder -> builder.setDefaultCookieStore(cookieStore));

        httpInterfaceManager.setHttpContextFilter(new HttpContextFilter() {
            @Override
            public void onContextOpen(HttpClientContext context) {
                context.setCookieStore(cookieStore);
            }

            @Override
            public void onContextClose(HttpClientContext context) {
                // Not used
            }

            @Override
            public void onRequest(HttpClientContext context, HttpUriRequest request, boolean isRepetition) {
                // set standard headers
                boolean isVideo = request.getURI().getPath().contains("video");
                fakeChrome(request, isVideo);

                String testCookie = context.getCookieStore().getCookies().stream()
                        .map(c -> c.getName() + '=' + c.getValue())
                        .collect(Collectors.joining("; "));

                request.setHeader("Cookie", testCookie);
                request.setHeader("Referer", "https://www.tiktok.com/");
            }

            @Override
            public boolean onRequestResponse(HttpClientContext context, HttpUriRequest request, HttpResponse response) {
                return false;
            }

            @Override
            public boolean onRequestException(HttpClientContext context, HttpUriRequest request, Throwable error) {
                return false;
            }
        });
    }

    public HttpInterface getHttpInterface() {
        return httpInterfaceManager.getInterface();
    }

    @Override
    public void close() throws Exception {
        httpInterfaceManager.close();
    }
}
