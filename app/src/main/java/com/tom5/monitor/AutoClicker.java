package com.tom5.monitor;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class AutoClicker extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // فحص إذا تغيرت حالة النافذة (ظهور نافذة "البدء الآن")
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || 
            event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            
            // التصحيح: استخدمنا getRootInActiveWindow بدل الاسم القديم
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            
            if (rootNode != null) {
                // البحث عن أزرار الموافقة والنقر عليها تلقائياً
                clickOnButton(rootNode, "البدء الآن");
                clickOnButton(rootNode, "Start now");
                clickOnButton(rootNode, "بدء الآن");
                rootNode.recycle();
            }
        }
    }

    private void clickOnButton(AccessibilityNodeInfo node, String text) {
        if (node == null) return;
        List<AccessibilityNodeInfo> nodes = node.findAccessibilityNodeInfosByText(text);
        if (nodes != null) {
            for (AccessibilityNodeInfo button : nodes) {
                if (button.isClickable()) {
                    button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                button.recycle();
            }
        }
    }

    @Override
    public void onInterrupt() {
        // توقف الخدمة
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // يتم استدعاؤه عند تشغيل الخدمة بنجاح
    }
}
