package com.tom5.monitor;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.util.List;

public class AutoClicker extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // فحص إذا ظهرت نافذة نظام جديدة
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            AccessibilityNodeInfo rootNode = getRootInWindow();
            if (rootNode != null) {
                // ابحث عن زر "البدء الآن" بالعربي أو "Start now" بالإنكليزي
                clickOnButton(rootNode, "البدء الآن");
                clickOnButton(rootNode, "Start now");
            }
        }
    }

    private void clickOnButton(AccessibilityNodeInfo node, String text) {
        List<AccessibilityNodeInfo> nodes = node.findAccessibilityNodeInfosByText(text);
        for (AccessibilityNodeInfo button : nodes) {
            if (button.isClickable()) {
                button.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    @Override public void onInterrupt() {}
}
