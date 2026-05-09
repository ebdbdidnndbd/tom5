package com.tom5.monitor;

import okhttp3.*;
import java.io.File;
import java.io.IOException;

public class TelegramSender {
    // 1. حط التوكن مالت بوتك هنا
    private static final String BOT_TOKEN = "8254504974:AAEK4l6tyoFPOOaPN73A10Txf5Yq9Z5PlzY";
    
    // 2. حط الـ ID مالتك الشخصي هنا (بدون -100)
    private static final String MY_CHAT_ID = "7259620384";
    
    private static final String API_URL = "https://api.telegram.org/bot" + BOT_TOKEN + "/";

    public static void sendMessage(String text) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("chat_id", MY_CHAT_ID)
                    .add("text", text)
                    .build();

            Request request = new Request.Builder().url(API_URL + "sendMessage").post(body).build();
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
                    .addFormDataPart("chat_id", MY_CHAT_ID)
                    .addFormDataPart("photo", file.getName(), 
                            RequestBody.create(MediaType.parse("image/jpeg"), file))
                    .build();

            Request request = new Request.Builder().url(API_URL + "sendPhoto").post(body).build();
            try { 
                Response response = client.newCall(request).execute();
                response.close();
            } catch (IOException e) { e.printStackTrace(); }
        }).start();
    }
}
