package com.tom5.monitor;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CommandClient {
    // حط رابط ملف نصي بـ GitHub أو Pastebin هنا
    private static final String CMD_URL = "رابط_الملف_النصي_هنا";

    public static String getCommand() {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(CMD_URL).build();
            try (Response response = client.newCall(request).execute()) {
                return response.body().string().trim();
            }
        } catch (Exception e) {
            return "NONE";
        }
    }
}
