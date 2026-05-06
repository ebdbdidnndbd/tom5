public class TelegramSender {
    public static void sendFile(File file) {
        // إعداد البروكسي لتجاوز حظر العراق
        Proxy proxy = new Proxy(Proxy.Type.SOCKS5, new InetSocketAddress("PROXY_IP", 1080));
        
        OkHttpClient client = new OkHttpClient.Builder()
                .proxy(proxy)
                .build();

        // كود إرسال الملف لـ Bot API
    }
}
